function switchTab(tab) {
  document.querySelectorAll('.tab-content').forEach(function(el) {
    el.style.display = 'none';
  });
  document.querySelectorAll('.tab-btn').forEach(function(el) {
    el.classList.remove('active');
  });
  var content = document.getElementById('tab-' + tab);
  if (content) content.style.display = 'block';
  var btn = document.querySelector('[data-tab="' + tab + '"]');
  if (btn) btn.classList.add('active');
}

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

  var flashes = document.querySelectorAll('.flash-message');
  flashes.forEach(function(flash) {
    setTimeout(function() {
      flash.style.transition = 'opacity 0.5s';
      flash.style.opacity = '0';
      setTimeout(function() { flash.remove(); }, 500);
    }, 4000);
  });
});
