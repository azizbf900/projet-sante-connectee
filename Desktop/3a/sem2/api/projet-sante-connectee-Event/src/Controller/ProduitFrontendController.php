<?php

namespace App\Controller;

use App\Entity\Categorie;
use App\Entity\Produit;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

class ProduitFrontendController extends AbstractController
{
    #[Route('/produit', name: 'frontend_index')]
    public function index(Request $request, EntityManagerInterface $entityManager): Response
    {
        // Récupérer toutes les catégories
        $categories = $entityManager->getRepository(Categorie::class)->findAll();

        // Récupérer la recherche de catégorie
        $categorieSearch = $request->query->get('categorie_search');
        if ($categorieSearch) {
            // Filtrer les catégories par le nom saisi
            $filtered_categories = $entityManager->getRepository(Categorie::class)
                ->createQueryBuilder('c')
                ->where('LOWER(c.name) LIKE :search')
                ->setParameter('search', '%' . strtolower($categorieSearch) . '%')
                ->getQuery()
                ->getResult();
        } else {
            // Si aucune recherche, afficher toutes les catégories
            $filtered_categories = $categories;
        }

        return $this->render('produit/frontend/index.html.twig', [
            'categories' => $categories,
            'filtered_categories' => $filtered_categories,
        ]);
    }

    #[Route('/categorie/{id}', name: 'frontend_categorie')]
    public function categorie(Categorie $categorie, Request $request, EntityManagerInterface $entityManager): Response
    {
        // Récupérer la recherche de produits
        $produitSearch = $request->query->get('produit_search');

        // Si un mot-clé est entré, effectuer un filtrage
        if ($produitSearch) {
            // Effectuer la recherche avec le mot-clé dans le nom ou la description des produits
            $produits = $entityManager->getRepository(Produit::class)
                ->createQueryBuilder('p')
                ->where('p.categorie = :categorie')
                ->andWhere('LOWER(p.nom) LIKE :search OR LOWER(p.description) LIKE :search')
                ->setParameter('categorie', $categorie)
                ->setParameter('search', '%' . strtolower($produitSearch) . '%')
                ->getQuery()
                ->getResult();
        } else {
            // Si aucun filtre n'est appliqué, afficher tous les produits
            $produits = $categorie->getProduits();
        }

        return $this->render('produit/frontend/categorie.html.twig', [
            'categorie' => $categorie,
            'produits' => $produits,
        ]);
    }

    #[Route('/pd/{id}', name: 'frontend_produit')]
    public function produit(Produit $produit): Response
    {
        return $this->render('produit/frontend/produit.html.twig', [
            'produit' => $produit,
        ]);
    }
}
