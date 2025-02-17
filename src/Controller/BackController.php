<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

final class BackController extends AbstractController
{
    #[Route('/back', name: 'app_back')]
    public function index(): Response
    {
        return $this->render('back/index.html.twig', [
            'controller_name' => 'BackController',
        ]);
    }




    #[Route('/post/new', name: 'post_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $post = new Post();
        $form = $this->createForm(PostType::class, $post);
        $form->handleRequest($request);

        if ($form->isSubmitted()) {
            dump('Formulaire soumis'); // Vérifie si le formulaire est soumis
        }
        
        if ($form->isSubmitted() && $form->isValid()) {
            // Récupération du fichier image
            $imageFile = $form->get('contenu')->getData();
        
            if ($imageFile) {
                $newFilename = uniqid().'.'.$imageFile->guessExtension();
        
                try {
                    $imageFile->move(
                        $this->getParameter('uploads_directory'),
                        $newFilename
                    );
                    $post->setContenu($newFilename);
                } catch (FileException $e) {
                    $this->addFlash('error', "Erreur lors de l'upload de l'image. Veuillez réessayer.");
                    return $this->redirectToRoute('post_new'); // Redirige vers le formulaire en cas d'erreur
                }
            }
        
            $post->setDatePublication(new \DateTime());
            $entityManager->persist($post);
            $entityManager->flush();
        
            $this->addFlash('success', 'Le post a été enregistré avec succès.');
            return $this->redirectToRoute('post_index');
        }

        return $this->render('post/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }
































}
