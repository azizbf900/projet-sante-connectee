<?php

namespace App\Form;

use App\Entity\DossierMedical;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\DateType;

class DossierMedicalType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('historiqueMaladies')
            ->add('allergies', ChoiceType::class, [
                'choices' => [
                    'Pollen' => 'Pollen',
                    'Poils de chat' => 'Poils de chat',
                    'Acariens' => 'Acariens',
                    'Lait' => 'Lait',
                    'Gluten' => 'Gluten',
                    'Fruits à coque' => 'Fruits à coque',
                    'Œufs' => 'Œufs',
                    'Poisson' => 'Poisson',
                    'Fruits de mer' => 'Fruits de mer',
                    'Médicaments' => 'Médicaments',
                ],
                
            ])
            ->add('traitementsEnCours', ChoiceType::class, [
                'choices' => [
                    'Antibiotiques' => 'Antibiotiques',
                    'Antidépresseurs' => 'Antidépresseurs',
                    'Antihistaminiques' => 'Antihistaminiques',
                    'Antihypertenseurs' => 'Antihypertenseurs',
                    'Antidiabétiques' => 'Antidiabétiques',
                    'Anti-inflammatoires' => 'Anti-inflammatoires',
                    'Anticoagulants' => 'Anticoagulants',
                    'Insuline' => 'Insuline',
                    'Contraceptifs' => 'Contraceptifs',
                    'Analgésiques' => 'Analgésiques',
                ],
                
            ])
            ->add('groupeSanguin', ChoiceType::class, [
                'choices' => [
                    'A+' => 'A+',
                    'A-' => 'A-',
                    'B+' => 'B+',
                    'B-' => 'B-',
                    'AB+' => 'AB+',
                    'AB-' => 'AB-',
                    'O+' => 'O+',
                    'O-' => 'O-',
                    'Inconnu' => 'Inconnu',
                    'Autre' => 'Autre',
                ],
            ])
            ->add('poids')
            ->add('taille')
            ->add('createdAt', DateType::class, [
                'widget' => 'single_text',
            ])
            ->add('patient', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'nom',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => DossierMedical::class,
        ]);
    }
}
