document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".like-post").forEach(button => {
        button.addEventListener("click", function() {
            let postId = this.getAttribute("data-id");

            fetch(`/post/${postId}/like`, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById(`likes-count-${postId}`).textContent = data.likes;
            });
        });
    });

    document.querySelectorAll(".dislike-post").forEach(button => {
        button.addEventListener("click", function() {
            let postId = this.getAttribute("data-id");

            fetch(`/post/${postId}/dislike`, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById(`dislikes-count-${postId}`).textContent = data.dislikes;
            });
        });
    });

    document.querySelectorAll(".like-comment").forEach(button => {
        button.addEventListener("click", function() {
            let commentId = this.getAttribute("data-id");

            fetch(`/commentaire/${commentId}/like`, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById(`likes-comment-${commentId}`).textContent = data.likes;
            });
        });
    });

    document.querySelectorAll(".dislike-comment").forEach(button => {
        button.addEventListener("click", function() {
            let commentId = this.getAttribute("data-id");

            fetch(`/commentaire/${commentId}/dislike`, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                }
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById(`dislikes-comment-${commentId}`).textContent = data.dislikes;
            });
        });
    });


    


});
