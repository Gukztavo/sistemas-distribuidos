package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import model.Pessoa;

public class AuthService {

    public static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("database");

    public static boolean authenticate(String email, String senha) {
        EntityManager manager = emf.createEntityManager();
        try {
            Pessoa pessoa = manager.createQuery("SELECT p FROM Pessoa p WHERE p.email = :email AND p.senha = :senha", Pessoa.class)
                    .setParameter("email", email)
                    .setParameter("senha", senha)
                    .getSingleResult();
            return pessoa != null;
        } catch (NoResultException e) {
            return false;
        } finally {
            manager.close();
        }
    }
}
