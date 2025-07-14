package br.com.email;

import br.com.email.dto.EmailRequest;

public interface EmailService {

    public void send(EmailRequest email);

}
