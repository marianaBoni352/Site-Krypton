package krypton;

import java.util.List;

/**
 * Todo o texto do museu fica aqui: para atualizar o conteúdo, basta editar este arquivo.
 * Trechos entre colchetes são campos que a equipe ainda precisa preencher.
 */
final class Conteudo {

    private Conteudo() {
    }

    /** Uma entrada das páginas de leitura. Parágrafos com "• " viram lista; com "## ", subtítulo. */
    record Secao(String rotulo, String titulo, List<String> paragrafos) {
    }

    /** Uma peça do museu. A foto é procurada em assets/robos/ e é opcional. */
    record Robo(int temporada, String jogo, String nome, String descricao, List<String> dados,
            String foto) {
    }

    /** Um item do rodapé. Sem link, é só informação; com link vazio, ainda falta configurar. */
    record Contato(String rotulo, String link) {
    }

    // ---- Rodapé ------------------------------------------------------------

    static final String LINK_FIRST = "https://www.firstinspires.org";

    static final List<Contato> CONTATOS = List.of(
            new Contato("Instagram", "https://www.instagramKryptonFRC#9487.com/"),
            new Contato("Email", "https://www.krypton.frc@gmail.com.com/"),
            new Contato("TikTok", "https://www.tiktokkrypton.frc.com/"),
            new Contato("YouTube", "https://www.youtubeKryptonFRC.com/"));

    // ---- História ----------------------------------------------------------

    static final List<Secao> HISTORIA = List.of(
            new Secao("O início", "Como tudo começou", List.of(
                    "A Krypton é a equipe #9487 da FIRST Robotics Competition, formada por estudantes apaixonados por engenharia, programação e trabalho em equipe.",
                    "A equipe nasceu a partir da vontade de transformar ideias em robôs e aprender construindo, testando e melhorando a cada ciclo de desenvolvimento. O nome Krypton foi escolhido para refletir a força, a criatividade e a ambição do grupo: um time que busca elevar a tecnologia e inspirar a próxima geração.")),
            new Secao("2024", "Temporada 2024 — CRESCENDO", List.of(
                    "Na primeira temporada registrada no museu, a Krypton começou a montar sua base: estrutura, atuadores, sensores e programação com foco em aprender com cada desafio.",
                    "Foi uma temporada de descobertas, com protótipos testados em bancada, ajustes de estratégia e muito esforço coletivo para transformar conceitos em um robô funcional.")),
            new Secao("2025", "Temporada 2025 — REEFSCAPE", List.of(
                    "Em março de 2025, a equipe competiu no regional de Brasília e mostrou evolução em desempenho, organização e integração entre as áreas.",
                    "O carro de competição ganhou maior estabilidade, a programação ficou mais robusta e a comunicação interna melhorou em cada etapa da temporada.")),
            new Secao("2026", "Temporada 2026 — REBUILT", List.of(
                    "Na temporada atual, a Krypton reforçou a cultura de melhoria contínua, com foco em autonomia, confiabilidade e trabalho em equipe.",
                    "Os próximos objetivos incluem evoluir a arquitetura do robô, consolidar o processo de desenvolvimento e continuar inspirando alunos e comunidade local através da FIRST.")));

    // ---- Robôs (da primeira temporada até a atual) ---------------------------

    static final List<Robo> ROBOS = List.of(
            new Robo(2024, "CRESCENDO", "KRYPTON I",
                    "O primeiro robô da equipe foi pensado para aprender na prática: confiável, versátil e preparado para testar conceitos de coleta, transporte e precisão.",
                    List.of("Tração: tração diferencial e desenvolvimento em bancada",
                            "Mecanismo: estrutura modular com elevação e movimentação de carga",
                            "Destaque: foco em autonomia e consistência"),
                    "robo-2024.png"),
            new Robo(2025, "REEFSCAPE", "KRYPTON II",
                    "A segunda geração adotou uma arquitetura mais agressiva, com maior foco em rapidez, manuseio e execução repetitiva dentro do jogo.",
                    List.of("Tração: sistema mais estável para movimentos rápidos",
                            "Mecanismo: melhorias na manipulação e no alinhamento de peças",
                            "Destaque: maior consistência em fases críticas da partida"),
                    "robo-2025.png"),
            new Robo(2026, "REBUILT", "KRYPTON III",
                    "O robô atual representa a evolução da equipe em mecânica, eletrônica e programação, com foco em eficiência e adaptabilidade.",
                    List.of("Tração: projeto otimizado para aceleração e estabilidade",
                            "Mecanismo: estrutura reforçada com atuação mais precisa",
                            "Destaque: integração entre automatização e estratégia de jogo"),
                    "robo-2026.png"));

    // ---- Premiações --------------------------------------------------------

    static final List<Secao> PREMIACOES = List.of(
            new Secao("2024", "Conquistas de 2024", List.of(
                    "• Reconhecimento de esforço e evolução tecnológica durante a temporada CRESCENDO.",
                    "• Destaque no trabalho em equipe e na organização do processo de construção.")),
            new Secao("2025", "Conquistas de 2025", List.of(
                    "• Participação destacada no regional de Brasília com evolução técnica clara.",
                    "• Melhoria no desempenho geral, com foco em confiabilidade e estratégia.")),
            new Secao("2026", "Conquistas de 2026", List.of(
                    "• Desenvolvimento de um robô mais robusto e eficiente para a temporada atual.",
                    "• Avanço contínuo em engenharia, programação e cooperação entre áreas.")));

    // ---- Áreas, mentores e integrantes ---------------------------------------

    static final List<Secao> AREAS = List.of(
            new Secao("Mecânica", "Mecânica", List.of(
                    "Projeta e constrói a estrutura e os mecanismos do robô, pensando em resistência, mobilidade e eficiência.",
                    "Na Krypton, a área mecânica trabalha em conjunto com eletrônica e programação para transformar cada ideia em uma solução funcional e confiável.")),
            new Secao("Eletrônica", "Eletrônica", List.of(
                    "Cuida da alimentação, da fiação e da ligação entre motores, sensores e controladores.",
                    "A eletrônica da Krypton é essencial para a integração do robô: ela garante que cada sistema funcione de forma precisa e segura.")),
            new Secao("Programação", "Programação", List.of(
                    "Desenvolve o código que controla o robô, tanto no período autônomo quanto no comandado pelos pilotos.",
                    "A equipe utiliza lógica, testes e refinamento contínuo para consolidar estratégias de jogo e melhorar a confiabilidade do sistema.")),
            new Secao("Marketing", "Marketing e comunicação", List.of(
                    "Conta a história da equipe, cuida das redes sociais e aproxima a Krypton de parceiros e da comunidade.",
                    "A comunicação é parte central da cultura da equipe: ela fortalece a identidade da Krypton e amplia o impacto do trabalho realizado.")),
            new Secao("Mentores", "Mentores", List.of(
                    "• Miguel Ramos — Engenharia de software.",
                    "• Guilherme Ferreira - Engenharia de software.",
                    "• Guilherme Pessoni - Engenharia Mecatrõnica.",
                    "• Nicolas Luz - CiÊncias de Computação.",
                    "• Hugo Hiroshi - Engenharia Mecânica.",
                    "• Daniel Watanabe - Sistema da informação.",
                    "• Aninha - Engenharia química.",
                    "• Gabriel - Engenharia Civil.")),
            new Secao("Integrantes", "Integrantes", List.of(
                    "## Temporada atual",
       // ---- MCI -------------------------------------------------------------    
                    "## Área de AE",
                    "• Laís — AE.",
                    "• Marcella — AE.",
                    "• Carol — AE.",
                    "• Leticia — AE.",
                    "• Victor — AE.",
     // ---- MCI -------------------------------------------------------------               
                    "## Área de MCI",
                    "• Lethcia— MCI.",
                    "• Thauany— MCI.",
                    "• Garcia — MCI.",
                    "• Saul — MCI.",
                    "• João Pedro — MCI.",
                    "• Mariana— MCI.",
                    "• Paulo— MCI.")));


    // ---- FIRST -------------------------------------------------------------

    static final List<Secao> FIRST = List.of(
            new Secao("O que é", "O que é a FIRST", List.of(
                    "FIRST (For Inspiration and Recognition of Science and Technology) é uma "
                            + "organização fundada por Dean Kamen em 1989 para inspirar jovens "
                            + "a seguir caminhos em ciência e tecnologia.")),
            new Secao("A FRC", "FIRST Robotics Competition", List.of(
                    "Na FRC, equipes de estudantes do ensino médio projetam, constroem e "
                            + "programam robôs de grande porte com a orientação de mentores.",
                    "A cada temporada, um novo desafio é revelado no Kickoff, em janeiro. Nas "
                            + "partidas, alianças de três robôs se enfrentam para cumprir os "
                            + "objetivos do jogo.")),
            new Secao("Valores", "Os valores da FIRST", List.of(
                    "## Gracious Professionalism",
                    "Competir com intensidade e, ao mesmo tempo, com respeito e gentileza.",
                    "## Coopertition",
                    "Cooperar e competir ao mesmo tempo: ajudar outra equipe também faz parte "
                            + "do jogo.")));
}
