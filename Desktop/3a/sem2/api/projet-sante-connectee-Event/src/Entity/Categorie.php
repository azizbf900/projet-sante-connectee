<?php

// src/Entity/Categorie.php

namespace App\Entity;

use App\Repository\CategorieRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: CategorieRepository::class)]
class Categorie
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    private ?string $name = null;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $imagePath = null;  // Champ imagePath ajouté (anciennement 'image')

    // Cette relation définit que la catégorie possède plusieurs produits
    #[ORM\OneToMany(mappedBy: "categorie", targetEntity: Produit::class)]
    private Collection $produits;

    public function __construct()
    {
        $this->produits = new ArrayCollection();
    }

    // Getter pour récupérer les produits liés à cette catégorie
    public function getProduits(): Collection
    {
        return $this->produits;
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getName(): ?string
    {
        return $this->name;
    }

    public function setName(string $name): static
    {
        $this->name = $name;

        return $this;
    }

    // Getter pour récupérer le chemin de l'image
    public function getImagePath(): ?string
    {
        return $this->imagePath;
    }

    // Setter pour définir le chemin de l'image
    public function setImagePath(?string $imagePath): self
    {
        $this->imagePath = $imagePath;
        return $this;
    }
}