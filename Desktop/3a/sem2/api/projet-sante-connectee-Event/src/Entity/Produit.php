<?php

namespace App\Entity;


use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
class Produit
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: "integer")]
    private $id;

    #[ORM\Column(type: "string", length: 255)]
    private $nom;

    #[ORM\Column(type: "text", nullable: true)]
    private $description;

    #[ORM\Column(type: "decimal", scale: 2)]
    private $prix;

    #[ORM\Column(type: "integer")]
    private $quantite;

    // Définir la relation ManyToOne avec l'entité Categorie
    #[ORM\ManyToOne(targetEntity: Categorie::class)]
    #[ORM\JoinColumn(name: "categorie_id", referencedColumnName: "id")]
    private $categorie;

    #[ORM\Column(type: 'string', length: 255, nullable: true)]
    private ?string $image_path = null;  // Champ imagePath ajouté (anciennement 'image')

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(string $nom): self
    {
        $this->nom = $nom;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(?string $description): self
    {
        $this->description = $description;
        return $this;
    }

    public function getPrix(): ?float
    {
        return $this->prix;
    }

    public function setPrix(float $prix): self
    {
        $this->prix = $prix;
        return $this;
    }

    public function getQuantite(): ?int
    {
        return $this->quantite;
    }

    public function setQuantite(int $quantite): self
    {
        $this->quantite = $quantite;
        return $this;
    }

    // Getter et Setter pour la propriété 'categorie'
    public function getCategorie(): ?Categorie
    {
        return $this->categorie;
    }

    public function setCategorie(?Categorie $categorie): self
    {
        $this->categorie = $categorie;
        return $this;
    }

    // Getter pour récupérer le chemin de l'image
    public function getimage_path(): ?string
    {
        return $this->image_path;
    }

    // Setter pour définir le chemin de l'image
    public function setimage_path(?string $imagePath): self
    {
        $this->image_path = $imagePath;
        return $this;
    }

}