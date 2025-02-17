<?php

namespace App\Form;

use App\Entity\DossierMedical;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DossierMedicalType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('historiqueMaladies')
            ->add('allergies')
            ->add('traitementsEnCours')
            ->add('groupeSanguin')
            ->add('poids')
            ->add('taille')
            ->add('createdAt', null, [
                'widget' => 'single_text',
            ])
            ->add('patient', EntityType::class, [
                'class' => User::class,
                'choice_label' => 'id',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => DossierMedical::class,
        ]);
    }
}
