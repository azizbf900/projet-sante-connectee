<?php

namespace App\Controller;

use App\Entity\Post;
use App\Entity\Commentaire;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;

class LikeController extends AbstractController
{
    #[Route('/post/{id}/like', name: 'post_like', methods: ['POST'])]
    public function likePost(Post $post, EntityManagerInterface $entityManager): JsonResponse
    {
        $post->setLikes($post->getLikes() + 1);
        $entityManager->flush();

        return new JsonResponse(['likes' => $post->getLikes()]);
    }

    #[Route('/post/{id}/dislike', name: 'post_dislike', methods: ['POST'])]
    public function dislikePost(Post $post, EntityManagerInterface $entityManager): JsonResponse
    {
        $post->setDislikes($post->getDislikes() + 1);
        $entityManager->flush();

        return new JsonResponse(['dislikes' => $post->getDislikes()]);
    }

    #[Route('/commentaire/{id}/like', name: 'commentaire_like', methods: ['POST'])]
    public function likeCommentaire(Commentaire $commentaire, EntityManagerInterface $entityManager): JsonResponse
    {
        $commentaire->setLikes($commentaire->getLikes() + 1);
        $entityManager->flush();

        return new JsonResponse(['likes' => $commentaire->getLikes()]);
    }

    #[Route('/commentaire/{id}/dislike', name: 'commentaire_dislike', methods: ['POST'])]
    public function dislikeCommentaire(Commentaire $commentaire, EntityManagerInterface $entityManager): JsonResponse
    {
        $commentaire->setDislikes($commentaire->getDislikes() + 1);
        $entityManager->flush();

        return new JsonResponse(['dislikes' => $commentaire->getDislikes()]);
    }
}
