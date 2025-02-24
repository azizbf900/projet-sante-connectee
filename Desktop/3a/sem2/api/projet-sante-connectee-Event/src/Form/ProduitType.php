<?php

// src/Form/ProduitType.php

namespace App\Form;

use App\Entity\Produit;
use App\Entity\Categorie;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;

use Symfony\Component\Validator\Constraints as Assert;

class ProduitType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options)
    {
        $builder
        ->add('nom', TextType::class, [
            'label' => 'Nom du produit',
            'constraints' => [
                new Assert\NotBlank([
                    'message' => 'Le nom du produit ne peut pas être vide.',
                ]),
                new Assert\Length([
                    'min' => 3,
                    'minMessage' => 'Le nom du produit doit contenir au moins {{ limit }} caractères.',
                ]),
            ]
        ])
        ->add('description', TextareaType::class, [
            'label' => 'Description du produit',
            'constraints' => [
                new Assert\NotBlank([
                    'message' => 'La description ne peut pas être vide.',
                ]),
                new Assert\Length([
                    'min' => 10,
                    'minMessage' => 'La description du produit doit contenir au moins {{ limit }} caractères.',
                ]),
            ]
        ])
        ->add('prix', NumberType::class, [
            'label' => 'Prix',
            'attr' => ['min' => 0, 'step' => '0.01'], // Autoriser les décimales
            'constraints' => [
                new Assert\NotBlank([
                    'message' => 'Le prix ne peut pas être vide.',
                ]),
                new Assert\GreaterThan([
                    'value' => 0,
                    'message' => 'Le prix doit être un nombre positif.',
                ]),
            ],
        ])
        ->add('quantite', IntegerType::class, [
            'label' => 'Quantité en stock',
            'constraints' => [
                new Assert\NotBlank([
                    'message' => 'La quantité ne peut pas être vide.',
                ]),
                new Assert\GreaterThan([
                    'value' => 0,
                    'message' => 'La quantité doit être supérieure à zéro.',
                ]),
            ]
        ])
        ->add('categorie', EntityType::class, [
            'label' => 'Catégorie',
            'class' => Categorie::class,
            'choice_label' => 'name',
            'choices' => $options['categories'],
            'constraints' => [
                new Assert\NotBlank([
                    'message' => 'Veuillez sélectionner une catégorie.',
                ]),
            ]
        ])
        ->add('image_path', FileType::class, [
            'label' => 'Image',
            'mapped' => false,  // Ne pas mapper ce champ directement à l'entité
            'required' => false,
        ])
        ->add('save', SubmitType::class, [
            'label' => 'Ajouter le produit',
        ]);
    }

    public function configureOptions(OptionsResolver $resolver)
    {
        $resolver->setDefaults([
            'data_class' => Produit::class,
            'categories' => [], // Option pour passer les catégories au formulaire
        ]);
    }
}
