<?php

namespace App\Controller;

use App\Entity\Commentaire;
use App\Form\CommentaireType;
use App\Repository\CommentaireRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use App\Entity\Post;

#[Route('/commentaire')]
class CommentaireController extends AbstractController
{
    #[Route('/', name: 'commentaire_index', methods: ['GET'])]
    public function index(CommentaireRepository $commentaireRepository): Response
    {
        return $this->render('commentaire/index.html.twig', [
            'commentaires' => $commentaireRepository->findAll(),
        ]);
    }



    #[Route('/commentaire/new/{id}', name: 'commentaire_new')]
    public function new(Request $request, Post $post, EntityManagerInterface $entityManager): Response
  {
    $commentaire = new Commentaire();
    $commentaire->setPost($post);
    $commentaire->setDateCommentaire(new \DateTime()); // Ajout automatique de la date

    $form = $this->createForm(CommentaireType::class, $commentaire);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->persist($commentaire);
        $entityManager->flush();

        return $this->redirectToRoute('post_show', ['id' => $post->getId()]);
    }

    return $this->render('commentaire/new.html.twig', [
        'form' => $form->createView(),
        'post' => $post,
    ]);
 }



    #[Route('/{id}', name: 'commentaire_show', methods: ['GET'])]
    public function show(Commentaire $commentaire): Response
    {
        return $this->render('commentaire/show.html.twig', [
            'commentaire' => $commentaire,
        ]);
    }

    #[Route('/{id}/edit', name: 'commentaire_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Commentaire $commentaire, EntityManagerInterface $entityManager): Response
{
    $form = $this->createForm(CommentaireType::class, $commentaire);
    $form->handleRequest($request);

    if ($form->isSubmitted() && $form->isValid()) {
        $entityManager->flush();

        return $this->redirectToRoute('post_show', ['id' => $commentaire->getPost()->getId()]);
    }

    return $this->render('commentaire/edit.html.twig', [
        'form' => $form->createView(),
        'commentaire' => $commentaire,
    ]);
 }

    #[Route('/{id}', name: 'commentaire_delete', methods: ['POST'])]
    public function delete(Request $request, Commentaire $commentaire, EntityManagerInterface $entityManager): Response
 {
    if ($this->isCsrfTokenValid('delete' . $commentaire->getId(), $request->request->get('_token'))) {
        $entityManager->remove($commentaire);
        $entityManager->flush();
    }

    return $this->redirectToRoute('post_show', ['id' => $commentaire->getPost()->getId()]);
 }
}
