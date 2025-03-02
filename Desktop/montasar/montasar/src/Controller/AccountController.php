<?php

namespace App\Controller;

use App\Entity\Account;
use App\Entity\Notification;
use App\Form\AccountType;
use App\Form\ResetPasswordRequestFormType;
use App\Form\VerifyResetCodeFormType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\RequestStack;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Psr\Log\LoggerInterface;
use Symfony\Component\Mailer\Exception\TransportExceptionInterface;
use Symfony\Component\Mailer\Transport;
use Symfony\Component\Mailer\Mailer;
use Symfony\Component\Process\Process;
use Symfony\Component\Process\Exception\ProcessFailedException;

final class AccountController extends AbstractController
{
    private $session;
    private $passwordHasher;
    private $logger;

    public function __construct(RequestStack $requestStack, UserPasswordHasherInterface $passwordHasher, LoggerInterface $logger)
    {
        $this->session = $requestStack->getSession();
        $this->passwordHasher = $passwordHasher;
        $this->logger = $logger;
    }

    /**
     * Helper method to create notifications.
     */
    private function createNotification(EntityManagerInterface $em, string $message): void
    {
        $notification = new Notification();
        $notification->setMessage($message);
        $em->persist($notification);
        $em->flush();
    }

    #[Route('/account', name: 'app_account_index', methods: ['GET'])]
    public function index(EntityManagerInterface $em): Response
    {
        $accounts = $em->getRepository(Account::class)->findAll();
        $notifications = $em->getRepository(Notification::class)->findAll();

        return $this->render('account/index.html.twig', [
            'accounts' => $accounts,
            'notifications' => $notifications,
        ]);
    }

    #[Route('/account/new', name: 'app_account_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em): Response
    {
        $account = new Account();
        $form = $this->createForm(AccountType::class, $account);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);

            $em->persist($account);
            $em->flush();

            // Create notification
            $currentUser = $this->session->get('user');
            if ($currentUser) {
                $this->createNotification($em, $currentUser->getNom() . ' has added a new user: ' . $account->getNom());
            } else {
                $this->addFlash('warning', 'Current user not found in session. Notification not created.');
            }

            $this->addFlash('success', 'Compte ajouté avec succès.');
            return $this->redirectToRoute('app_account_index'); // Redirect to /account
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

            // Create notification
            $currentUser = $this->session->get('user');
            if ($currentUser) {
                $this->createNotification($em, $currentUser->getNom() . ' has edited user: ' . $account->getNom());
            } else {
                $this->addFlash('warning', 'Current user not found in session. Notification not created.');
            }

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
        // Create notification
        $currentUser = $this->session->get('user');
        if ($currentUser) {
            $this->createNotification($em, $currentUser->getNom() . ' has deleted user: ' . $account->getNom());
        } else {
            $this->addFlash('warning', 'Current user not found in session. Notification not created.');
        }

        $em->remove($account);
        $em->flush();

        $this->addFlash('success', 'Compte supprimé avec succès.');
        return $this->redirectToRoute('app_account_index');
    }

    #[Route('/notifications', name: 'app_notifications')]
    public function notifications(EntityManagerInterface $em): Response
    {
        $notifications = $em->getRepository(Notification::class)->findAll();
    
        return $this->render('notifications.html.twig', [
            'notifications' => $notifications,
        ]);
    }

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
            $hashedPassword = $this->passwordHasher->hashPassword($account, $account->getPassword());
            $account->setPassword($hashedPassword);

            $entityManager->persist($account);
            $entityManager->flush();

            $this->addFlash('success', 'Votre compte a été créé avec succès.');
            return $this->redirectToRoute('app_front_signin');
        }

        return $this->render('account/signin.html.twig', [
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
        $this->session->clear();
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

    #[Route('/reset-password', name: 'app_reset_password')]
    public function resetPassword(Request $request, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ResetPasswordRequestFormType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $email = $form->get('email')->getData();
            $account = $entityManager->getRepository(Account::class)->findOneBy(['mail' => $email]);

            if ($account) {
                // Generate a reset code
                $resetCode = random_int(100000, 999999);
                $account->setResetCode($resetCode);

                // Initialize the mailer (same as in testEmail)
                $transport = Transport::fromDsn('smtp://montasar@gmail.com:lhdwgkhxdlahhnud@smtp.gmail.com:465');
                $mailer = new Mailer($transport);

                // Send reset code via email
                $emailMessage = (new Email())
                    ->from('firas.guesmi001@gmail.com')
                    ->to($email)
                    ->subject('Your Password Reset Code')
                    ->text('Your password reset code is: ' . $resetCode);

                try {
                    $mailer->send($emailMessage);
                    $this->logger->info('Password reset email sent to ' . $email);

                    // Save the reset code to the database
                    $entityManager->flush();

                    // Redirect to the reset code verification page
                    return $this->redirectToRoute('app_verify_reset_code', ['email' => $email]);
                } catch (TransportExceptionInterface $e) {
                    $this->logger->error('Failed to send password reset email: ' . $e->getMessage());
                    $this->addFlash('danger', 'Failed to send password reset email.');
                }
            } else {
                $this->addFlash('danger', 'Email not found.');
            }
        }

        return $this->render('account/reset_password_request.html.twig', [
            'requestForm' => $form->createView(),
        ]);
    }

    #[Route('/verify-reset-code', name: 'app_verify_reset_code', methods: ['GET', 'POST'])]
    public function verifyResetCode(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher, LoggerInterface $logger): Response
    {
        $form = $this->createForm(VerifyResetCodeFormType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $email = $request->query->get('email');
            $resetCode = $form->get('resetCode')->getData(); // Assuming this is an integer from your form
            $newPassword = $form->get('newPassword')->getData();

            // Find the account by email
            $account = $entityManager->getRepository(Account::class)->findOneBy(['mail' => $email]);

            if ($account) {
                $storedResetCode = $account->getResetCode();

                // Log for debugging
                $logger->info('Submitted reset code: ' . $resetCode . ' (type: ' . gettype($resetCode) . ')');
                $logger->info('Stored reset code: ' . $storedResetCode . ' (type: ' . gettype($storedResetCode) . ')');

                // Compare reset codes (ensure they are the same type)
                if ($storedResetCode === $resetCode) {
                    // Hash and set the new password
                    $hashedPassword = $passwordHasher->hashPassword($account, $newPassword);
                    $account->setPassword($hashedPassword);
                    $account->setResetCode(null); // Clear the reset code

                    // Persist changes to the database
                    $entityManager->flush();

                    $this->addFlash('success', 'Password updated successfully.');
                    return $this->redirectToRoute('app_front_signin');
                } else {
                    $this->addFlash('danger', 'Invalid reset code.');
                    $logger->info('Reset code mismatch');
                }
            } else {
                $this->addFlash('danger', 'Account not found.');
                $logger->info('Account not found for email: ' . $email);
            }
        } else {
            if ($form->isSubmitted()) {
                $logger->info('Form submitted but invalid');
                // Log form errors for debugging
                foreach ($form->getErrors(true) as $error) {
                    $logger->error('Form error: ' . $error->getMessage());
                }
            }
        }

        return $this->render('account/verify_reset_code.html.twig', [
            'verifyForm' => $form->createView(),
            'email' => $request->query->get('email'), // Pass email to template if needed
        ]);
    }

    #[Route('/chat', name: 'app_chat', methods: ['GET', 'POST'])]
    public function chat(Request $request, EntityManagerInterface $entityManager): Response
    {
        // Ensure the user is authenticated
        if (!$this->session->get('user')) {
            $this->addFlash('danger', 'You must be logged in to access the chat.');
            return $this->redirectToRoute('app_front_signin');
        }

        $session = $request->getSession();
        $conversation = $session->get('conversation', []);

        if ($request->isMethod('POST')) {
            $message = $request->request->get('message');

            if (!empty($message)) {
                try {
                    $command = ['ollama', 'run', 'qwen2.5-coder:7b', $message];
                    $process = new Process($command);
                    $process->run();

                    if (!$process->isSuccessful()) {
                        $this->logger->error('Ollama error: ' . $process->getErrorOutput());
                        $this->addFlash('danger', 'Error processing your message. Please try again.');
                    } else {
                        $response = trim($process->getOutput());
                        // Add to conversation history
                        $conversation[] = [
                            'question' => $message,
                            'answer' => $response
                        ];
                        // Save updated conversation
                        $session->set('conversation', $conversation);
                    }
                } catch (\Exception $e) {
                    $this->logger->error('Chat error: ' . $e->getMessage());
                    $this->addFlash('danger', 'An error occurred while processing your request.');
                }
            }
        }

        return $this->render('chat.html.twig', [
            'conversation' => $conversation
        ]);
    }


    #[Route('/test-email')]
    public function testEmail(MailerInterface $mailer): Response
    {
        $transport = Transport::fromDsn('smtp://montasar@gmail.com:lhdwgkhxdlahhnud@smtp.gmail.com:465');

        // Create a Mailer object
        $mailer = new Mailer($transport);

        // Create an Email object
        $email = (new Email())
            ->from('montasar@gmail.com')
            ->to('firasguesmi93806411@gmail.com')
            ->subject('Test Email')
            ->text('This is a test email.');

        $mailer->send($email);
        try {
            // Send email
            $mailer->send($email);

            // Display custom successful message
            die('<style> * { font-size: 100px; color: #444; background-color: #4eff73; } </style><pre><h1>&#127881;Email sent successfully!</h1></pre>');
           // return new Response('Email sent!');
        } catch (TransportExceptionInterface $e) {
            // Display custom error message
            die('<style>* { font-size: 100px; color: #fff; background-color: #ff4e4e; }</style><pre><h1>&#128544;Error!</h1></pre>');
            //return new Response('Email didnt sent!');
            // Display real errors
            # echo '<pre style="color: red;">', print_r($e, TRUE), '</pre>';
        }
    }
}