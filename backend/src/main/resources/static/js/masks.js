function validarCPF(cpf) {
  cpf = cpf.replace(/\D/g, '');
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
  var soma = 0, resto;
  for (var i = 1; i <= 9; i++) soma += parseInt(cpf.substring(i - 1, i)) * (11 - i);
  resto = (soma * 10) % 11;
  if (resto === 10 || resto === 11) resto = 0;
  if (resto !== parseInt(cpf.substring(9, 10))) return false;
  soma = 0;
  for (var i = 1; i <= 10; i++) soma += parseInt(cpf.substring(i - 1, i)) * (12 - i);
  resto = (soma * 10) % 11;
  if (resto === 10 || resto === 11) resto = 0;
  return resto === parseInt(cpf.substring(10, 11));
}

function validarCNPJ(cnpj) {
  cnpj = cnpj.replace(/\D/g, '');
  if (cnpj.length !== 14 || /^(\d)\1{13}$/.test(cnpj)) return false;
  var soma = 0, peso = [5,4,3,2,9,8,7,6,5,4,3,2];
  for (var i = 0; i < 12; i++) soma += parseInt(cnpj[i]) * peso[i];
  var resto = soma % 11;
  if (resto < 2) resto = 0; else resto = 11 - resto;
  if (resto !== parseInt(cnpj[12])) return false;
  soma = 0; peso = [6,5,4,3,2,9,8,7,6,5,4,3,2];
  for (var i = 0; i < 13; i++) soma += parseInt(cnpj[i]) * peso[i];
  resto = soma % 11;
  if (resto < 2) resto = 0; else resto = 11 - resto;
  return resto === parseInt(cnpj[13]);
}

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

  var authForms = document.querySelectorAll('.auth-form');
  authForms.forEach(function(form) {
    form.addEventListener('submit', function(e) {
      var cpfField = form.querySelector('.mask-cpf');
      if (cpfField && cpfField.value && !validarCPF(cpfField.value)) {
        e.preventDefault(); alert('CPF inválido. Verifique o número informado.'); return;
      }
      var cnpjField = form.querySelector('.mask-cnpj');
      if (cnpjField && cnpjField.value && !validarCNPJ(cnpjField.value)) {
        e.preventDefault(); alert('CNPJ inválido. Verifique o número informado.'); return;
      }
      var senha = form.querySelector('input[name="senha"]');
      var confirmacao = form.querySelector('input[name="confirmacao"]');
      if (senha && confirmacao && senha.value !== confirmacao.value) {
        e.preventDefault(); alert('As senhas não conferem.'); return;
      }
    });
  });

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
