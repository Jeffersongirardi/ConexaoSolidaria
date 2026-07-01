function aplicarMascara(input, mascara) {
  input.addEventListener('input', function () {
    var value = this.value.replace(/\D/g, '');
    var resultado = '';
    var idx = 0;
    for (var i = 0; i < mascara.length && idx < value.length; i++) {
      if (mascara[i] === '9') {
        resultado += value[idx++];
      } else {
        resultado += mascara[i];
      }
    }
    this.value = resultado;
  });
}

document.addEventListener('DOMContentLoaded', function () {
  var cpf = document.querySelector('.mask-cpf');
  if (cpf) aplicarMascara(cpf, '999.999.999-99');

  var cnpj = document.querySelector('.mask-cnpj');
  if (cnpj) aplicarMascara(cnpj, '99.999.999/9999-99');

  var cep = document.querySelector('.mask-cep');
  if (cep) aplicarMascara(cep, '99999-999');

  var phone = document.querySelectorAll('.mask-phone');
  phone.forEach(function (el) { aplicarMascara(el, '(99) 99999-9999'); });

  var card = document.querySelector('.mask-card');
  if (card) aplicarMascara(card, '9999 9999 9999 9999');

  var cvv = document.querySelector('.mask-cvv');
  if (cvv) aplicarMascara(cvv, '999');

  var expiry = document.querySelector('.mask-expiry');
  if (expiry) aplicarMascara(expiry, '99/99');

  var uploadInputs = document.querySelectorAll('input[type="file"][accept="image/*"]');
  uploadInputs.forEach(function (input) {
    input.addEventListener('change', function () {
      var preview = this.closest('.upload-dropzone') || this.parentElement;
      var existing = preview.querySelector('.preview-imagem');
      if (!existing) {
        existing = document.createElement('div');
        existing.className = 'preview-imagem';
        existing.style.cssText = 'display:flex;gap:8px;flex-wrap:wrap;margin-top:8px;';
        preview.appendChild(existing);
      }
      existing.innerHTML = '';
      Array.from(this.files).slice(0, 5).forEach(function (file) {
        var reader = new FileReader();
        reader.onload = function (e) {
          var img = document.createElement('img');
          img.src = e.target.result;
          img.style.cssText = 'width:80px;height:80px;object-fit:cover;border-radius:6px;';
          existing.appendChild(img);
        };
        reader.readAsDataURL(file);
      });
    });
  });
});
