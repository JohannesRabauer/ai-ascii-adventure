package dev.rabauer.ai_ascii_adventure.domain;

public class Game {
    public static final String INITIAL_STORY_PROMPT = "";

    public static final String DEFAULT_CHOICES_PROMPT = """
            
            """;


    public static final String DEFAULT_TOOL_PROMPT = """
            
            """;
    private final Hero hero;
    private final Story story;
    private Long entityId;
    private String title;

    public Game(Hero hero, Story story) {
        this.hero = hero;
        this.story = story;
    }

    public Hero getHero() {
        return hero;
    }

    public Story getStory() {
        return story;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
}
