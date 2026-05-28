package com.lz.atendimentoclinica;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ViaCepService {
    @GET("{cep}/json/")
    Call<CepResponse> getEndereco(@Path("cep") String cep);
}