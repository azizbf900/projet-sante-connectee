<?php

namespace App\Controller;

use App\Entity\Categorie;
use App\Entity\Produit;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class ProduitFrontendController extends AbstractController
{
    #[Route('/', name: 'frontend_index')]
    public function index(EntityManagerInterface $entityManager): Response
    {
        $categories = $entityManager->getRepository(Categorie::class)->findAll();

        return $this->render('produit/frontend/index.html.twig', [
            'categories' => $categories,
        ]);
    }

    #[Route('/categorie/{id}', name: 'frontend_categorie')]
    public function categorie(Categorie $categorie): Response
    {
        return $this->render('produit/frontend/categorie.html.twig', [
            'categorie' => $categorie,
            'produits' => $categorie->getProduits(),
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
