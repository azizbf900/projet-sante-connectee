<?php

namespace App\Entity;

use App\Repository\AccountRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: AccountRepository::class)]
class Account
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le nom ne peut pas être vide.")]
    private ?string $nom = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le prénom ne peut pas être vide.")]
    private ?string $prenom = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: "L'âge ne peut pas être vide.")]
    #[Assert\Type(type: "integer", message: "L'âge doit être un nombre.")]
    #[Assert\Positive(message: "L'âge doit être un nombre positif.")]
    #[Assert\Range(min: 1, max: 150, notInRangeMessage: "L'âge doit être compris entre 1 et 150.")]
    private ?int $age = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "L'email ne peut pas être vide.")]
    #[Assert\Email(message: "L'adresse email '{{ value }}' n'est pas valide.")]
    private ?string $mail = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: "Le numéro de téléphone ne peut pas être vide.")]
    #[Assert\Type(type: "integer", message: "Le numéro de téléphone doit être un nombre.")]
    #[Assert\Positive(message: "Le numéro de téléphone doit être un nombre positif.")]
    private ?int $phone = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le rôle ne peut pas être vide.")]
    private ?string $role = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le mot de passe ne peut pas être vide.")]
    #[Assert\Length(min: 6, minMessage: "Le mot de passe doit comporter au moins {{ limit }} caractères.")]
    private ?string $password = null;

    // Getters and Setters
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

    public function getPrenom(): ?string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): self
    {
        $this->prenom = $prenom;
        return $this;
    }

    public function getAge(): ?int
    {
        return $this->age;
    }

    public function setAge(int $age): self
    {
        $this->age = $age;
        return $this;
    }

    public function getMail(): ?string
    {
        return $this->mail;
    }

        // Getter and Setter for password
        public function getPassword(): ?string
        {
            return $this->password;
        }
    public function setMail(string $mail): self
    {
        $this->mail = $mail;
        return $this;
    }

    public function getPhone(): ?int
    {
        return $this->phone;
    }

    public function setPhone(int $phone): self
    {
        $this->phone = $phone;
        return $this;
    }

    public function getRole(): ?string
    {
        return $this->role;
    }

    public function setRole(string $role): self
    {
        $this->role = $role;
        return $this;
    }


    public function setPassword(string $password): self
    {
        $this->password = $password;
        return $this;
    }
}
