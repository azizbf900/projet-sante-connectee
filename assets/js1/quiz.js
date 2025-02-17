function selectOption(element) {
    if (!element) {
        console.warn("selectOption: No element provided.");
        return;
    }
    const quizContainer = element.closest('.quiz_container');
    if (!quizContainer) return;
    quizContainer.querySelectorAll('.quiz_option').forEach(opt => opt.classList.remove('focused'));
    element.classList.add('focused');
}
document.querySelectorAll('.quiz_option').forEach(option => {
    option.addEventListener('click', function () {
        selectOption(this);
    });
});









