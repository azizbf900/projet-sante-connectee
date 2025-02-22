<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use App\Repository\PostRepository;
use Symfony\Component\HttpFoundation\Request;
use Doctrine\ORM\EntityManagerInterface;
use App\Entity\Post;
use App\Form\PostType;

final class PostController extends AbstractController
{
   


    #[Route('/post', name: 'post_index')]
     public function index(PostRepository $postRepository): Response
   {
        return $this->render('post/index.html.twig', [
           'posts' => $postRepository->findAll(),
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


  #[Route('/post/{id}', name: 'post_show', requirements: ['id' => '\d+'])]
  public function show(Post $post): Response
  {
        return $this->render('post/show.html.twig', [
          'post' => $post,
        ]);
  }

   

  #[Route('/post/{id}/edit', name: 'post_edit', methods: ['GET', 'POST'])]
public function edit(Request $request, Post $post, EntityManagerInterface $entityManager): Response
{
    $form = $this->createForm(PostType::class, $post);
    $form->handleRequest($request);

    if ($form->isSubmitted()) {
        dump('Formulaire soumis'); // Vérifie si le formulaire est soumis
    }

    if ($form->isSubmitted() && $form->isValid()) {
        dump('Formulaire valide'); // Vérifie si le formulaire est valide

        // Récupération du fichier image
        $imageFile = $form->get('contenu')->getData();

        if ($imageFile) {
            $newFilename = uniqid().'.'.$imageFile->guessExtension();

            try {
                $imageFile->move(
                    $this->getParameter('uploads_directory'), // Défini dans services.yaml
                    $newFilename
                );
                // Supprime l'ancienne image si elle existe
                if ($post->getContenu()) {
                    $oldFilePath = $this->getParameter('uploads_directory') . '/' . $post->getContenu();
                    if (file_exists($oldFilePath)) {
                        unlink($oldFilePath);
                    }
                }
                $post->setContenu($newFilename);
            } catch (FileException $e) {
                $this->addFlash('error', "Erreur lors de l'upload de l'image.");
                return $this->redirectToRoute('post_edit', ['id' => $post->getId()]);
            }
        }

        // Enregistrement en base de données
        $entityManager->flush();

        $this->addFlash('success', 'Le post a été modifié avec succès.');
        return $this->redirectToRoute('post_index');
    }

    return $this->render('post/edit.html.twig', [
        'form' => $form->createView(),
        'post' => $post, // Passe le post à la vue pour afficher des détails supplémentaires
    ]);
}

#[Route('/post/{id}/delete', name: 'post_delete', methods: ['POST'])]
public function delete(Request $request, Post $post, EntityManagerInterface $entityManager): Response
{
    // Supprimer les commentaires associés au post
    foreach ($post->getCommentaires() as $commentaire) {
        $entityManager->remove($commentaire);
    }

    if ($this->isCsrfTokenValid('delete' . $post->getId(), $request->request->get('_token'))) {
        $entityManager->remove($post);
        $entityManager->flush();
        $this->addFlash('success', 'Post supprimé avec succès.');
    }

    return $this->redirectToRoute('post_index');
}



}



