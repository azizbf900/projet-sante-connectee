<?php

namespace App\Controller;

use App\Entity\Anonce;
use App\Form\AnonceType;
use App\Repository\AnonceRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/anonce')]
final class AnonceController extends AbstractController
{
    #[Route(name: 'app_anonce_index', methods: ['GET'])]
    public function index(AnonceRepository $anonceRepository): Response
    {
        return $this->render('anonce/index.html.twig', [
            'anonces' => $anonceRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_anonce_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $anonce = new Anonce();
        $form = $this->createForm(AnonceType::class, $anonce);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($anonce);
            $entityManager->flush();

            return $this->redirectToRoute('app_anonce_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('anonce/new.html.twig', [
            'anonce' => $anonce,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_anonce_show', methods: ['GET'])]
    public function show(Anonce $anonce): Response
    {
        return $this->render('anonce/show.html.twig', [
            'anonce' => $anonce,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_anonce_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Anonce $anonce, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(AnonceType::class, $anonce);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_anonce_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('anonce/edit.html.twig', [
            'anonce' => $anonce,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_anonce_delete', methods: ['POST'])]
    public function delete(Request $request, Anonce $anonce, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$anonce->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($anonce);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_anonce_index', [], Response::HTTP_SEE_OTHER);
    }
}
