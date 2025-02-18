<?php

namespace App\Controller;

use App\Entity\Account;
use App\Form\AccountType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

final class AccountController extends AbstractController
{

    private $session;
    private $passwordHasher;


    public function __construct(RequestStack $requestStack , UserPasswordHasherInterface $passwordHasher)
    {
        $this->session = $requestStack->getSession();
        $this->passwordHasher = $passwordHasher;

    }

    //crud
    #[Route('/account', name: 'app_account_index', methods: ['GET'])]
    public function index(EntityManagerInterface $em ): Response
    {
        $accounts = $em->getRepository(Account::class)->findAll();

        return $this->render('account/index.html.twig', [
            'accounts' => $accounts,
        ]);
    }

    #[Route('/account/new', name: 'app_account_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em , UserPasswordHasherInterface $passwordEncoder): Response
    {
        $account = new Account();
        $form = $this->createForm(AccountType::class, $account);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);

            $em->persist($account);
            $em->flush();

            $this->addFlash('success', 'Compte ajouté avec succès.');

            return $this->redirectToRoute('app_account_index');
        }

        return $this->render('account/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/account/edit/{id}', name: 'app_account_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Account $account, EntityManagerInterface $em): Response
    {
        $form = $this->createForm(AccountType::class, $account);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);

            $em->flush();

            $this->addFlash('success', 'Compte modifié avec succès.');

            return $this->redirectToRoute('app_account_index');
        }

        return $this->render('account/edit.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/account/delete/{id}', name: 'app_account_delete', methods: ['GET', 'POST'])]
    public function delete(Account $account, EntityManagerInterface $em): Response
    {
        // Remove the account from the database
        $em->remove($account);
        $em->flush();

        // Add a flash message
        $this->addFlash('success', 'Compte supprimé avec succès.');

        // Redirect to the account index page
        return $this->redirectToRoute('app_account_index');
    }





     //lien page home front 
    #[Route('/home', name: 'HomeFront')]
    public function Home(): Response
    {
        return $this->render('account/Home.html.twig', [
            'controller_name' => 'AccountController',
        ]);
    }


    #[Route('/signin', name: 'app_front_signin')]
    public function signin(Request $request, EntityManagerInterface $entityManager): Response
    {
        $account = new Account();
        $form = $this->createForm(AccountType::class, $account);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
    
            // Save user to database
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);

            $entityManager->persist($account);
            $entityManager->flush();

            $this->addFlash('success', 'Votre compte a été créé avec succès.');

            return $this->redirectToRoute('app_front_signin'); // Redirect after successful registration
        }

        return $this->render('Account/signin.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/login', name: 'app_login', methods: ['POST'])]
    public function login(Request $request, EntityManagerInterface $em): Response
    {
        $mail = $request->request->get('mail');
        $password = $request->request->get('password');
    
        $user = $em->getRepository(Account::class)->findOneBy(['mail' => $mail]);
    
        if (!$user) {
            $this->addFlash('danger', 'Email invalide.');
            return $this->redirectToRoute('app_front_signin');
        }
    
        // Verify if the entered password matches the stored hashed password
        if (!$this->passwordHasher->isPasswordValid($user, $password)) {
            $this->addFlash('danger', 'Mot de passe invalide.');
            return $this->redirectToRoute('app_front_signin');
        }

        $this->session->set('user', $user);    

        if ($user->getRole() === 'admin') {
            return $this->redirectToRoute('app_account_index');
        } else {
            return $this->redirectToRoute('HomeFront');
        }
    }
    


    #[Route('/logout', name: 'app_logout')]
    public function logout(): Response
    {
        $this->session->clear(); // Clears all session data
        return $this->redirectToRoute('app_front_signin');
    }

    
    #[Route('/manage-account', name: 'app_manage_account', methods: ['GET', 'POST'])]
public function manageAccount(Request $request, EntityManagerInterface $em): Response
{
    $user = $this->session->get('user');
    if (!$user) {
        $this->addFlash('danger', 'You must be logged in to manage your account.');
        return $this->redirectToRoute('app_front_signin');
    }

    $account = $em->getRepository(Account::class)->find($user->getId());
    if (!$account) {
        $this->addFlash('danger', 'User not found.');
        return $this->redirectToRoute('app_front_signin');
    }

    $form = $this->createForm(AccountType::class, $account);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
       
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);
        
        $em->flush();
        $this->session->set('user', $account);
        $this->addFlash('success', 'Your account has been updated successfully.');

        return $this->redirectToRoute('app_manage_account');
    }

    return $this->render('account/ManageAccount.html.twig', [
        'form' => $form->createView(),
        'account' => $account,
    ]);
}



    



}
