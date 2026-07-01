document.addEventListener('DOMContentLoaded', function () {
  var form = document.getElementById('needForm');
  if (form) {
    var btn = form.querySelector('button[type="submit"]');
    if (btn) {
      btn.addEventListener('click', function (e) {
        var titulo = form.querySelector('[name="titulo"]');
        if (titulo && !titulo.value.trim()) {
          e.preventDefault();
          alert('Preencha o título da necessidade.');
        }
      });
    }
  }
});
