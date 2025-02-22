<?php
// src/Controller/ProduitController.php

namespace App\Controller;

use App\Entity\Produit;
use App\Entity\Categorie;
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
    public function index(
        ProduitRepository $produitRepository,
        CategorieRepository $categorieRepository,
        Request $request,
        EntityManagerInterface $entityManager
    ): Response {
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
            $entityManager->persist($produit);
            $entityManager->flush();

            // Rediriger vers la page des produits après la création
            return $this->redirectToRoute('produit_index');
        }

        // Rendre la vue avec la liste des produits et le formulaire
        return $this->render('produit/backend/index.html.twig', [
            'produits' => $produitRepository->findAll(),
            'form' => $form->createView(),
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request): Response
    {
        // Créer un nouvel objet Produit
        $produit = new Produit();

        // Récupérer les catégories depuis la base de données
        $categorieRepository = $this->entityManager->getRepository(Categorie::class);
        $categories = $categorieRepository->findAll();

        // Créer le formulaire en passant les catégories
        $form = $this->createForm(ProduitType::class, $produit, [
            'categories' => $categories,  // Passer les catégories au formulaire
        ]);

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
        return $this->render('produit/backend/new.html.twig', [
            'form' => $form->createView(),
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(Produit $produit): Response 
    {
        return $this->render('produit/backend/_form.html.twig', [
            'Produit' => $produit,
        ]);
    }

    #[Route('/produit/{id}/edit', name: 'produit_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Produit $produit, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(ProduitType::class, $produit);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();
            $this->addFlash('success', 'Produit mis à jour avec succès');
            return $this->redirectToRoute('produit_index');
        }

        return $this->render('produit/backend/edit.html.twig', [
            'form' => $form->createView(),
            'produit' => $produit,
        ]);
    }

    #[Route('/produit/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, Produit $produit): Response
    {
        // Validation du token CSRF
        if ($this->isCsrfTokenValid('delete'.$produit->getId(), $request->request->get('_token'))) {
            // Suppression du produit en utilisant l'EntityManager
            $this->entityManager->remove($produit);
            $this->entityManager->flush();

            $this->addFlash('success', 'Produit supprimé avec succès.');
        }

        return $this->redirectToRoute('produit_index');
    }


}