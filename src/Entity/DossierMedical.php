<?php

namespace App\Entity;

use App\Repository\DossierMedicalRepository;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: DossierMedicalRepository::class)]
class DossierMedical
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\OneToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    
    private ?User $patient = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(
        max: 1000,
        maxMessage: "L'historique des maladies ne peut pas dépasser {{ limit }} caractères."
    )]
    private ?string $historiqueMaladies = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(
        max: 500,
        maxMessage: "Les allergies ne peuvent pas dépasser {{ limit }} caractères."
    )]
    private ?string $allergies = null;

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(
        max: 500,
        maxMessage: "Les traitements en cours ne peuvent pas dépasser {{ limit }} caractères."
    )]
    private ?string $traitementsEnCours = null;

    #[ORM\Column(length: 10, nullable: true)]
    #[Assert\Choice(
        choices: ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'],
        message: "Veuillez entrer un groupe sanguin valide."
    )]
    private ?string $groupeSanguin = null;

    #[ORM\Column(type: 'float', nullable: true)]
    #[Assert\Positive(message: "Le poids doit être un nombre positif.")]
    #[Assert\LessThanOrEqual(value: 500, message: "Le poids doit être inférieur ou égal à {{ compared_value }} kg.")]
    private ?float $poids = null;

    #[ORM\Column(type: 'float', nullable: true)]
    #[Assert\Positive(message: "La taille doit être un nombre positif.")]
    #[Assert\LessThanOrEqual(value: 3, message: "La taille doit être inférieure ou égale à {{ compared_value }} mètres.")]
    private ?float $taille = null;

    #[ORM\Column(type: 'datetime')]
    #[Assert\NotNull(message: "La date de création est obligatoire.")]
    private ?\DateTimeInterface $createdAt = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getPatient(): ?User
    {
        return $this->patient;
    }

    public function setPatient(User $patient): static
    {
        $this->patient = $patient;
        return $this;
    }

    public function getHistoriqueMaladies(): ?string
    {
        return $this->historiqueMaladies;
    }

    public function setHistoriqueMaladies(?string $historiqueMaladies): static
    {
        $this->historiqueMaladies = $historiqueMaladies;
        return $this;
    }

    public function getAllergies(): ?string
    {
        return $this->allergies;
    }

    public function setAllergies(?string $allergies): static
    {
        $this->allergies = $allergies;
        return $this;
    }

    public function getTraitementsEnCours(): ?string
    {
        return $this->traitementsEnCours;
    }

    public function setTraitementsEnCours(?string $traitementsEnCours): static
    {
        $this->traitementsEnCours = $traitementsEnCours;
        return $this;
    }

    public function getGroupeSanguin(): ?string
    {
        return $this->groupeSanguin;
    }

    public function setGroupeSanguin(?string $groupeSanguin): static
    {
        $this->groupeSanguin = $groupeSanguin;
        return $this;
    }

    public function getPoids(): ?float
    {
        return $this->poids;
    }

    public function setPoids(?float $poids): static
    {
        $this->poids = $poids;
        return $this;
    }

    public function getTaille(): ?float
    {
        return $this->taille;
    }

    public function setTaille(?float $taille): static
    {
        $this->taille = $taille;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }
}
