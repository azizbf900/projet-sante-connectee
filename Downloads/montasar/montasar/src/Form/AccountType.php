<?php
namespace App\Form;

use App\Entity\Account;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;

class AccountType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Nom', 'class' => 'form-control']
            ])
            ->add('prenom', TextType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Prénom', 'class' => 'form-control']
            ])
            ->add('age', IntegerType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Âge', 'class' => 'form-control']
            ])
            ->add('mail', EmailType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Email', 'class' => 'form-control']
            ])
            ->add('phone', TextType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Numéro de téléphone', 'class' => 'form-control']
            ])
            ->add('role', ChoiceType::class, [
                'choices' => [
                    'User' => 'user',
                    'Admin' => 'admin',
                ],
                'expanded' => false,
                'multiple' => false,
                'placeholder' => 'Sélectionnez un rôle',
                'required' => false,
                'attr' => ['class' => 'form-control']
            ])
            ->add('password', PasswordType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Mot de Passe', 'class' => 'form-control']
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Account::class,
        ]);
    }
}
