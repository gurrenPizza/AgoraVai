package com.example.alberto.agoravai.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.alberto.agoravai.DAO.ConfiguracaoFirebase;
import com.example.alberto.agoravai.Entidades.Usuarios;
import com.example.alberto.agoravai.Helper.Base64Custom;
import com.example.alberto.agoravai.Helper.Preferencias;
import com.example.alberto.agoravai.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class CadastroActivity extends AppCompatActivity {

    private EditText edtCadEmail;
    private EditText edtCadNome;
    private EditText edtCadSobrenome;
    private EditText edtCadSenha;
    private EditText edtCadConfirmaSenha;
    private EditText edtCadAniversario;
    private RadioButton rbMasculino;
    private RadioButton rbFeminino;
    private Button btnGravar;
    private Usuarios usuarios;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        edtCadEmail = (EditText) findViewById(R.id.edtCadEmail);
        edtCadNome = (EditText) findViewById(R.id.edtCadNome);
        edtCadSobrenome = (EditText) findViewById(R.id.edtCadSobrenome);
        edtCadSenha = (EditText) findViewById(R.id.edtCadSenha);
        edtCadConfirmaSenha = (EditText) findViewById(R.id.edtCadConfirmaSenha);
        edtCadAniversario = (EditText) findViewById(R.id.edtCadAniversario);
        rbFeminino = (RadioButton) findViewById(R.id.rbCadFeminino);
        rbMasculino = (RadioButton) findViewById(R.id.rbCadMasculino);
        btnGravar = (Button) findViewById(R.id.btnGravar);

        btnGravar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if (edtCadSenha.getText().toString().equals(edtCadConfirmaSenha.getText().toString())){

                    usuarios = new Usuarios();
                    //recupera todos os valores nos campos preenchidos na tela pelo usuário
                    //e armazena no objeto de usuário
                    usuarios.setNome(edtCadNome.getText().toString());
                    usuarios.setSobrenome(edtCadSobrenome.getText().toString());
                    usuarios.setEmail(edtCadEmail.getText().toString());
                    usuarios.setSenha(edtCadSenha.getText().toString());
                    usuarios.setAniversario(edtCadAniversario.getText().toString());
                    //faz a checagem de qual botão de radio está selecionado para preencher o campo sexo
                    if(rbFeminino.isChecked()){
                        usuarios.setSexo("Feminino");
                    }
                    else{
                        usuarios.setSexo("Masculino");
                    }
                    cadastrarUsuario();
                }
                else{
                    Toast.makeText(CadastroActivity.this, "As senhas não são correspondentes",Toast.LENGTH_LONG).show();
                }

            }

        });

    }
    //efetua o cadastro de usuários com e-mail e senha, além de criar o nó próprio do usuário no
    //realtime database
    public void cadastrarUsuario(){

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuarios.getEmail(),
                usuarios.getSenha()
        ).addOnCompleteListener(CadastroActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(CadastroActivity.this, "Usuário cadastrado com MUITO sucesso!",Toast.LENGTH_LONG).show();

                    String identificadorUsuario = Base64Custom.codificarString(usuarios.getEmail());
                    FirebaseUser usuarioFirebase = task.getResult().getUser();
                    usuarios.setId(identificadorUsuario);
                    usuarios.salvar();

                    Preferencias preferencias = new Preferencias(CadastroActivity.this);
                    preferencias.salvarUsuarioPreferencias(identificadorUsuario, usuarios.getNome());
                    abrirLoginUsuario();
                }
                else{
                    String erroExcecao = "";

                    try{
                        throw task.getException();
                    }catch(FirebaseAuthWeakPasswordException e){
                        erroExcecao = "digite uma senha mais forte com ao menos 8 caracteres contendo letras e números";
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        erroExcecao = "o e-mail digitado é inválido, digite um novo e-mail";
                    }catch(FirebaseAuthUserCollisionException e){
                        erroExcecao = "esse e-mail já está cadastrado no sistema";
                    }catch(Exception e){
                        erroExcecao = "erro ao efetuar o cadastro!";
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this, "Erro: " + erroExcecao,Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void abrirLoginUsuario(){
        Intent abrirTelaLogin = new Intent(CadastroActivity.this, LoginActivity.class);
        startActivity(abrirTelaLogin);
        finish();
    }
}
