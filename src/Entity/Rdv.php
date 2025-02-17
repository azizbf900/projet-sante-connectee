<?php

namespace App\Entity;

use App\Repository\RdvRepository;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: RdvRepository::class)]
class Rdv
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: 'datetime')]
    #[Assert\NotNull(message: "La date et l'heure ne peuvent pas être nulles.")]
    #[Assert\Type(\DateTimeInterface::class, message: "La valeur doit être une date et heure valide.")]
    #[Assert\GreaterThan('today', message: "La date et l'heure doivent être ultérieures à la date d'aujourd'hui.")]
    private ?\DateTimeInterface $dateHeure = null;

    #[ORM\Column(length: 20)]
    #[Assert\NotBlank(message: "Le statut est obligatoire.")]
    #[Assert\Choice(
        choices: ['en_ligne', 'presentiel'],
        message: "Le statut doit être 'en_ligne' ou 'presentiel'."
    )]
    private ?string $statut = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotNull(message: "Le patient est obligatoire.")]
    private ?User $patient = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotNull(message: "Le médecin est obligatoire.")]
    private ?User $medecin = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDateHeure(): ?\DateTimeInterface
    {
        return $this->dateHeure;
    }

    public function setDateHeure(\DateTimeInterface $dateHeure): static
    {
        $this->dateHeure = $dateHeure;
        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): static
    {
        $this->statut = $statut;
        return $this;
    }

    public function getPatient(): ?User
    {
        return $this->patient;
    }

    public function setPatient(?User $patient): static
    {
        $this->patient = $patient;
        return $this;
    }

    public function getMedecin(): ?User
    {
        return $this->medecin;
    }

    public function setMedecin(?User $medecin): static
    {
        $this->medecin = $medecin;
        return $this;
    }
}
