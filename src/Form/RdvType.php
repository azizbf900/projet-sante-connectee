<?php

namespace App\Form;

use App\Entity\Rdv;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\OptionsResolver\OptionsResolver;

class RdvType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('dateHeure', null, [
                'widget' => 'single_text',
            ])
            ->add('statut', ChoiceType::class, [
                'choices' => [
                    'En ligne' => 'en_ligne',
                    'Présentiel' => 'presentiel',
                ],
                'expanded' => true,  // Utilise des boutons radio
                'multiple' => false, // Une seule option possible
                'label_attr' => ['style' => 'display: block;'], // Force l'affichage vertical
            ])
            
            
            ->add('patient', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'nom',
            ])
            ->add('medecin', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'nom',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Rdv::class,
        ]);
    }
}
