package com.lz.atendimentoclinica;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiService {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Interface para retorno do CEP
    public interface CepCallback {
        void onSuccess(String logradouro, String bairro, String cidade, String uf);
        void onError(String mensagem);
    }

    public static void buscarCep(String cep, CepCallback callback) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");

        if (cepLimpo.length() != 8) {
            callback.onError("CEP inválido");
            return;
        }
        String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError("Erro de conexão"));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);

                    if (json.has("erro")) {
                        mainHandler.post(() -> callback.onError("CEP não encontrado"));
                        return;
                    }
                    String logradouro = json.optString("logradouro", "");
                    String bairro = json.optString("bairro", "");
                    String cidade = json.optString("localidade", "");
                    String uf = json.optString("uf", "");

                    mainHandler.post(() -> callback.onSuccess(logradouro, bairro, cidade, uf));
                } catch (Exception e) {
                    mainHandler.post(() -> callback.onError("Erro ao processar CEP"));
                }
            }
        });
    }

    // ADICIONADO: Algoritmo de Validação Oficial de CNPJ (14 dígitos)
    public static boolean validarCnpj(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");

        if (cnpjLimpo.length() != 14) return false;

        // Elimina CNPJ com todos os dígitos iguais (ex: 00000000000000)
        if (cnpjLimpo.matches("(\\d)\\1{13}")) return false;

        try {
            // Cálculo do 1º Dígito Verificador
            int sm = 0;
            int peso = 2;
            for (int i = 11; i >= 0; i--) {
                int num = Character.getNumericValue(cnpjLimpo.charAt(i));
                sm += (num * peso);
                peso++;
                if (peso == 10) peso = 2;
            }
            int r = sm % 11;
            int dig13 = (r < 2) ? 0 : (11 - r);

            // Cálculo do 2º Dígito Verificador
            sm = 0;
            peso = 2;
            for (int i = 12; i >= 0; i--) {
                int num = Character.getNumericValue(cnpjLimpo.charAt(i));
                sm += (num * peso);
                peso++;
                if (peso == 10) peso = 2;
            }
            r = sm % 11;
            int dig14 = (r < 2) ? 0 : (11 - r);

            // Verifica se os dígitos calculados batem com os digitados
            return (dig13 == Character.getNumericValue(cnpjLimpo.charAt(12))) &&
                    (dig14 == Character.getNumericValue(cnpjLimpo.charAt(13)));

        } catch (Exception e) {
            return false;
        }
    }
}