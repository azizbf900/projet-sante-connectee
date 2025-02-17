<?php
// src/Controller/ProduitController.php

namespace App\Controller;

use App\Entity\Produit;
use App\Form\ProduitType;
use App\Repository\ProduitRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Response;
use Doctrine\ORM\EntityManagerInterface;
use App\Repository\CategorieRepository;


use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[Route('/produit', name: 'produit_')]
class ProduitController extends AbstractController
{
    private $entityManager;

    // Injection du EntityManagerInterface dans le constructeur
    public function __construct(EntityManagerInterface $entityManager)
    {
        $this->entityManager = $entityManager;
    }
    #[Route('/', name: 'index', methods: ['GET'])]
    public function index(ProduitRepository $produitRepository, CategorieRepository $categorieRepository, Request $request): Response
    {
         // Créer un nouvel objet Produit
         $produit = new Produit();
        
         // Récupérer toutes les catégories
         $categories = $categorieRepository->findAll();
         // Préparer les catégories sous forme de tableau clé-valeur
         $categoriesArray = [];
         foreach ($categories as $categorie) {
             $categoriesArray[$categorie->getName()] = $categorie;
         }
 
         // Créer le formulaire en passant les catégories comme option
         $form = $this->createForm(ProduitType::class, $produit, [
             'categories' => $categoriesArray,
         ]);
 
         // Gérer la soumission du formulaire
         $form->handleRequest($request);
         if ($form->isSubmitted() && $form->isValid()) {
             $entityManager = $this->getDoctrine()->getManager();
             $entityManager->persist($produit);
             $entityManager->flush();
 
             return $this->redirectToRoute('app_produit');
         }
 
         return $this->render('produit/index.html.twig', [
             'produits' => $produitRepository->findAll(),
             'form' => $form->createView(),
         ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        // Créer un nouvel objet Produit
        $produit = new Produit();

        // Créer le formulaire
        $form = $this->createForm(ProduitType::class, $produit);

        // Gérer la soumission du formulaire
        $form->handleRequest($request);
        
        // Vérifier si le formulaire a été soumis et est valide
        if ($form->isSubmitted() && $form->isValid()) {
            // Persist et flush avec l'EntityManager injecté
            $this->entityManager->persist($produit);
            $this->entityManager->flush();

            // Ajouter un message flash pour informer de la réussite
            $this->addFlash('success', 'Produit créé avec succès.');

            // Rediriger vers la page de liste des produits
            return $this->redirectToRoute('produit_index');
        }

        // Rendre la vue avec le formulaire
        return $this->render('produit/create.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(Produit $produit): Response 
    {
        return $this->render('produit/show.html.twig', [
            'Produit' => $produit,
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Produit $produit): Response
    {
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $this->getDoctrine()->getManager()->flush();

            $this->addFlash('success', 'Produit modifié avec succès.');
            return $this->redirectToRoute('produit_edit', ['id' => $produit->getId()]);
        }

        return $this->render('produit/edit.html.twig', [
            'produit' => $produit,
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, Produit $produit): Response
    {
        if ($this->isCsrfTokenValid('delete'.$produit->getId(), $request->request->get('_token'))) {
            $entityManager = $this->getDoctrine()->getManager();
            $entityManager->remove($produit);
            $entityManager->flush();

            $this->addFlash('success', 'Produit supprimé avec succès.');
        }

        return $this->redirectToRoute('produit_index');
    }

}
