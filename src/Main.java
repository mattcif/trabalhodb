import java.io.IOException;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/sistema_clientes";
        String user = "root";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false); // Inicia a transação

            // Exemplo de ID do cliente e dados novos
            int idCliente = 1;
            String novoNome = "Cliente Alterado";
            double novoLimite = 5000.00;
            int versaoAtual = 1; // Valor carregado previamente

            // Verifica se os dados ainda são válidos
            String verificaQuery = "SELECT version FROM cliente WHERE idcliente = ?";
            try (PreparedStatement verificaStmt = conn.prepareStatement(verificaQuery)) {
                verificaStmt.setInt(1, idCliente);
                ResultSet rs = verificaStmt.executeQuery();

                if (rs.next()) {
                    int versaoBanco = rs.getInt("version");

                    if (versaoBanco == versaoAtual) {
                        // Dados estão iguais, pode atualizar
                        String updateQuery = "UPDATE cliente SET nome = ?, limite = ?, version = version + 1 WHERE idcliente = ? AND version = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, novoNome);
                            updateStmt.setDouble(2, novoLimite);
                            updateStmt.setInt(3, idCliente);
                            updateStmt.setInt(4, versaoAtual);

                            int linhasAfetadas = updateStmt.executeUpdate();

                            if (linhasAfetadas > 0) {
                                // Peça confirmação ao usuário
                                System.out.println("Deseja confirmar a alteração? (S/N)");
                                char confirmacao = (char) System.in.read();

                                if (confirmacao == 'S' || confirmacao == 's') {
                                    conn.commit(); // Confirma a transação
                                    System.out.println("Alteração confirmada!");
                                } else {
                                    conn.rollback(); // Cancela a transação
                                    System.out.println("Alteração cancelada!");
                                }
                            } else {
                                System.out.println("Erro ao atualizar o cliente.");
                                conn.rollback();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // Dados já foram alterados
                        System.out.println("Os dados do cliente foram alterados por outro usuário.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback(); // Cancela a transação em caso de erro
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
