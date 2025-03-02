<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\Range;

class VerifyResetCodeFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('resetCode', IntegerType::class, [
                'attr' => ['placeholder' => 'Enter reset code', 'class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'Reset code is required.']),
                    new Range([
                        'min' => 100000,
                        'max' => 999999,
                        'notInRangeMessage' => 'Reset code must be a 6-digit number.',
                    ]),
                ],
            ])
            ->add('newPassword', PasswordType::class, [
                'attr' => ['placeholder' => 'Enter new password', 'class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'New password is required.']),
                    new Length([
                        'min' => 8,
                        'minMessage' => 'Password must be at least 8 characters long.',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => null, // Disable data mapping
        ]);
    }
}