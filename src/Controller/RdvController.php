<?php

namespace App\Controller;

use App\Entity\Rdv;
use App\Form\RdvType;
use App\Repository\RdvRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Knp\Component\Pager\PaginatorInterface;


#[Route('/rdv')]
final class RdvController extends AbstractController
{
    #[Route(name: 'app_rdv_index', methods: ['GET'])]
public function index(RdvRepository $rdvRepository, PaginatorInterface $paginator, Request $request): Response
{
    $query = $rdvRepository->findBy([], ['dateHeure' => 'DESC']); // Exemple d'ordre, facultatif

    $pagination = $paginator->paginate(
        $query,
        $request->query->getInt('page', 1), // Page actuelle, par défaut 1
        5 // Nombre de résultats par page
    );

    return $this->render('rdv/index.html.twig', [
        'rdvs' => $pagination,
    ]);
}


    #[Route('/page',name: 'app_rdv_page', methods: ['GET'])]
    public function page(RdvRepository $rdvRepository): Response
    {
        return $this->render('rdv/Page.html.twig', [
            'rdvs' => $rdvRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_rdv_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $rdv = new Rdv();
        $form = $this->createForm(RdvType::class, $rdv);
        $form->handleRequest($request);
    
        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($rdv);
            $entityManager->flush();
    
            // Rendu direct de la page de confirmation après soumission réussie
            return $this->render('rdv/confirmation.html.twig', [
                'rdv' => $rdv, // Vous passez ici l'objet $rdv à la page de confirmation
            ]);
        }
    
        return $this->render('rdv/new.html.twig', [
            'rdv' => $rdv,
            'form' => $form->createView(),
        ]);
    }
    


        #[Route('/{id}', name: 'app_rdv_show', methods: ['GET'])]
        public function show(Rdv $rdv): Response
        {
            return $this->render('rdv/show.html.twig', [
                'rdv' => $rdv,
            ]);
        }

    #[Route('/{id}/edit', name: 'app_rdv_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Rdv $rdv, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(RdvType::class, $rdv);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_rdv_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('rdv/edit.html.twig', [
            'rdv' => $rdv,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_rdv_delete', methods: ['POST'])]
    public function delete(Request $request, Rdv $rdv, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$rdv->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($rdv);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_rdv_index', [], Response::HTTP_SEE_OTHER);
    }
}
