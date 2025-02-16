<?php

namespace App\Controller;

use App\Entity\Account;
use App\Form\AccountType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\HttpFoundation\RequestStack;

final class AccountController extends AbstractController
{

    private $session;

    public function __construct(RequestStack $requestStack)
    {
        $this->session = $requestStack->getSession();
    }

    //crud
    #[Route('/account', name: 'app_account_index', methods: ['GET'])]
    public function index(EntityManagerInterface $em): Response
    {
        $accounts = $em->getRepository(Account::class)->findAll();

        return $this->render('account/index.html.twig', [
            'accounts' => $accounts,
        ]);
    }

    #[Route('/account/new', name: 'app_account_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $account = new Account();
        $form = $this->createForm(AccountType::class, $account);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
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
    public function signin(): Response
    {
        return $this->render('Account/signin.html.twig', [
        ]);
    }

    #[Route('/login', name: 'app_login', methods: ['POST'])]
    public function login(Request $request, EntityManagerInterface $em): Response
    {
        $mail = $request->request->get('mail');
        $password = $request->request->get('password');
    
        $user = $em->getRepository(Account::class)->findOneBy(['mail' => $mail]);
    
        if (!$user || $user->getPassword() != $password) {
            dd('error Utilisateur non trouvé.');
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

    
#[Route('/signup', name: 'app_register', methods: ['GET', 'POST'])]
public function SignUp(Request $request, EntityManagerInterface $em): Response
{
    if ($request->isMethod('POST')) {
        $nom = $request->request->get('nom');
        $prenom = $request->request->get('prenom');
        $age = $request->request->get('age');
        $email = $request->request->get('email');
        $phone = $request->request->get('phone');
        $password = $request->request->get('password');
        
        // Create a new Account instance
        $account = new Account();
        $account->setNom($nom);
        $account->setPrenom($prenom);
        $account->setAge($age);
        $account->setMail($email);
        $account->setPhone($phone);
        $account->setPassword($password);
        $account->setRole('user');

        $em->persist($account);
        $em->flush();

        $this->session->set('user', $account);    
        $this->addFlash('success', 'Compte ajouté avec succès.');
        return $this->redirectToRoute('HomeFront');
    }

    // Render the sign-up page
    return $this->render('Account/signin.html.twig');
}


    



}
