document.addEventListener('DOMContentLoaded', function() {
    console.log('Script charCount.js chargé'); // Log pour vérifier que le script est exécuté

    const textarea = document.getElementById('legende');
    const charCount = document.getElementById('charCount');

    if (!textarea || !charCount) {
        console.error('Éléments non trouvés : textarea ou charCount'); // Log si les éléments ne sont pas trouvés
        return;
    }

    console.log('Textarea et charCount trouvés'); // Log si les éléments sont trouvés

    const maxLength = 1000;

    // Initialiser le compteur avec la valeur actuelle du texte
    charCount.textContent = maxLength - textarea.value.length;

    textarea.addEventListener('input', function() {
        const remaining = maxLength - textarea.value.length;
        console.log('Caractères restants :', remaining); // Log pour vérifier la valeur de remaining

        charCount.textContent = remaining;

        // Optionnel : Changer la couleur du compteur si la limite est proche
        if (remaining <= 20) {
            charCount.style.color = 'red';
        } else {
            charCount.style.color = 'black';
        }

        // Optionnel : Tronquer le texte si la limite est dépassée
        if (remaining < 0) {
            textarea.value = textarea.value.substring(0, maxLength);
            charCount.textContent = 0;
        }
    });
});