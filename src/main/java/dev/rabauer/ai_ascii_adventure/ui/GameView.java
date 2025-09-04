package dev.rabauer.ai_ascii_adventure.ui;

import com.google.adk.agents.RunConfig;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Part;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import dev.rabauer.ai_ascii_adventure.GameOverManager;
import dev.rabauer.ai_ascii_adventure.ai.AiService;
import dev.rabauer.ai_ascii_adventure.ai.AssistantWithMemory;
import dev.rabauer.ai_ascii_adventure.ai.HeroUiCommunicator;
import dev.rabauer.ai_ascii_adventure.domain.Game;
import dev.rabauer.ai_ascii_adventure.domain.Hero;
import dev.rabauer.ai_ascii_adventure.domain.Story;
import dev.rabauer.ai_ascii_adventure.domain.StoryPart;
import dev.rabauer.ai_ascii_adventure.persistence.GamePersistenceService;

import java.util.ArrayList;
import java.util.Optional;

import static dev.rabauer.ai_ascii_adventure.ai.AiService.*;
import static dev.rabauer.ai_ascii_adventure.domain.Game.INITIAL_STORY_PROMPT;

@Route(value = "game", layout = MainLayout.class)
public class GameView extends SplitLayout implements GameOverManager, HasUrlParameter<String> {
    private final TextArea txtStory = new TextArea();
    private final TextArea txtAsciiArt = new TextArea();
    private final AiService aiService;
    private final GamePersistenceService gamePersistenceService;
    private AssistantWithMemory chatModel;
    private ProgressBar prbHealth;
    private ProgressBar prbMana;
    private HeroUiCommunicator heroCommunicator;
    private Span spnInventory;
    private Game game;


    public GameView(AiService aiService, GamePersistenceService gamePersistenceService) {
        this.aiService = aiService;
        this.gamePersistenceService = gamePersistenceService;

        this.setSizeFull();
        this.addToPrimary(createAsciiArt());
        this.addToSecondary(createStoryComponent());
    }

    public void showGameOver(boolean fail) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        if (fail) {
            dialog.setHeaderTitle("Game Over");
            dialog.add(new Span("You died. Try again soon."));
        } else {
            dialog.setHeaderTitle("Victory!");
            dialog.add(new Span("You finished the game! Congratulations!"));
        }
        dialog.open();
    }

    private Component createAsciiArt() {
        txtAsciiArt.setSizeFull();
        txtAsciiArt.setTitle("Graphics");
        txtAsciiArt.getStyle().set("font-family", "'Courier New', monospace");

        ProgressBarComponentPair pairHealth = generateHealthManaBar("Health", "red");
        prbHealth = pairHealth.progressBar();
        ProgressBarComponentPair pairMana = generateHealthManaBar("Mana", "blue");
        prbMana = pairMana.progressBar();

        HorizontalLayout hlStatusBar = new HorizontalLayout(pairHealth.component, pairMana.component);
        hlStatusBar.setWidthFull();
        hlStatusBar.setSpacing(false);

        NativeLabel inventoryTitle = new NativeLabel("Inventory");
        spnInventory = new Span();
        spnInventory.setWidthFull();
        spnInventory.setHeight("100px");
        VerticalLayout vlInventory = new VerticalLayout(inventoryTitle, spnInventory);
        vlInventory.setSpacing(false);
        vlInventory.setHeight("100px");
        vlInventory.setWidthFull();

        return new VerticalLayout(txtAsciiArt, hlStatusBar, vlInventory);
    }

    private ProgressBarComponentPair generateHealthManaBar(String title, String color) {
        ProgressBar newProgressBar = new ProgressBar(0, 100, 100);
        newProgressBar.setWidthFull();
        newProgressBar.setHeight("20px");
        newProgressBar.getStyle().set("--lumo-primary-color", color);

        NativeLabel newLabel = new NativeLabel(title);

        return new ProgressBarComponentPair(newProgressBar, new VerticalLayout(newLabel, newProgressBar));
    }

    private Component createStoryComponent() {
        txtStory.setTitle("Story");
        txtStory.setSizeFull();

        com.vaadin.flow.component.textfield.NumberField numInput = new com.vaadin.flow.component.textfield.NumberField();
        numInput.setTitle("Prompt");
        numInput.setValueChangeMode(ValueChangeMode.EAGER);
        numInput.setMin(1);
        numInput.setMax(4);
        numInput.setStep(1);
        numInput.setWidth("100px");
        numInput.addKeyDownListener(Key.ENTER, event -> {
            Double value = numInput.getValue();
            if (value != null && value >= 1 && value <= 4) {
                generateNewStoryPart(String.valueOf(value.intValue()));
            }
        });
        Button btnSendPrompt = new Button("Send");
        btnSendPrompt.addClickListener(k -> {
            Double value = numInput.getValue();
            if (value != null && value >= 1 && value <= 4) {
                generateNewStoryPart(String.valueOf(value.intValue()));
            }
        });
        HorizontalLayout hlUserInput = new HorizontalLayout(numInput, btnSendPrompt);

        return new VerticalLayout(txtStory, hlUserInput);
    }

    private void generateNewStoryPart(String textPrompt) {
        this.txtStory.clear();

        UI current = UI.getCurrent();
        LlmAgents relevantAgents = aiService.createRelevantAgents();

        RunConfig runConfig = RunConfig.builder().build();
        InMemoryRunner runner = new InMemoryRunner(relevantAgents.refinementLoop());


        Session session =
                runner
                        .sessionService()
                        .createSession("AIAdventure", "1")
                        .blockingGet();

        runner.runAsync(session, com.google.genai.types.Content.fromParts(Part.fromText("Let's go!")), runConfig).blockingForEach(event -> {
            if (event.finalResponse()) {
                current.access(() -> txtStory.setValue(txtStory.getValue() + event.stringifyContent()));
            }
        });
//        aiService.generateNewStoryPart(
//                this.chatModel,
//                this.game.getEntityId(),
//                textPrompt,
//                newText ->
//                        current.access(
//                                () ->
//                                        txtStory.setValue(txtStory.getValue() + newText)
//                        ),
//                completeResponse -> current.access(() -> handleFinishedStoryPart(completeResponse.aiMessage().text()))
//        );
    }

    private void handleFinishedStoryPart(String finishedStory) {
        StoryPart storyPart = new StoryPart(finishedStory);
        this.game.getStory().storyParts().add(storyPart);

        //Is this the first part?
        if (this.game.getStory().storyParts().size() == 1) {
            generateGameTitle(finishedStory);
        }

        generateAsciiArt(finishedStory);
        generateFunctionCalls(finishedStory);
    }

    private void handleFinishedAllAiActions() {
        this.gamePersistenceService.saveGameAndEnsureGameId(this.game);
        ComponentUtil.fireEvent(UI.getCurrent(), new GameChangeEvent(this, false));

    }

    private void generateFunctionCalls(String finishedStory) {
        aiService.generateNewChatResponseWithoutMemory(
                aiService.createChatModelWithTools(this.heroCommunicator),
                Game.DEFAULT_TOOL_PROMPT.formatted(finishedStory),
                responseWithTitle -> handleFinishedAllAiActions()
        );
    }

    private void generateAsciiArt(String finishedStory) {
        txtAsciiArt.clear();

        UI current = UI.getCurrent();
        aiService.generateNewChatResponseWithoutMemory(
                aiService.createChatModelWithoutMemory(),
                EXTRACT_IMAGE_TITLE_PROMPT.formatted(finishedStory),
                responseWithTitle -> {
                    current.access(() -> txtAsciiArt.setTitle(responseWithTitle));
                    aiService.generateNewChatResponseWithoutMemory(
                            aiService.createChatModelWithoutMemory(),
                            CREATE_ASCII_ART_PROMPT.formatted(responseWithTitle),
                            response -> current.access(() -> txtAsciiArt.setValue(response))
                    );
                }
        );
    }

    private void generateGameTitle(String firstStoryPart) {
        aiService.generateNewChatResponseWithoutMemory(
                aiService.createChatModelWithoutMemory(),
                EXTRACT_GAME_TITLE_PROMPT.formatted(firstStoryPart),
                responseWithTitle ->
                {
                    this.game.setTitle(responseWithTitle);
                    this.gamePersistenceService.saveGameAndEnsureGameId(this.game);
                    ComponentUtil.fireEvent(UI.getCurrent(), new GameChangeEvent(this, false));
                }
        );
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            loadGame(parameter);
        } else {
            startNewGame();
        }
    }

    private void loadGame(String gameId) {
        Optional<Game> loadedGame = this.gamePersistenceService.loadGame(gameId);
        if (loadedGame.isEmpty()) {
            throw new IllegalStateException("Game with id " + gameId + " not found.");
        }
        this.game = loadedGame.get();
        this.heroCommunicator = new HeroUiCommunicator(
                game, this.prbHealth, this.prbMana, this.spnInventory, this
        );

        String lastStoryPart = this.game.getStory().storyParts().get(this.game.getStory().storyParts().size() - 1).text();
        txtStory.setValue(lastStoryPart);
        generateAsciiArt(lastStoryPart);

        this.chatModel = aiService.createChatModelWithMemory();
    }

    private void startNewGame() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        dialog.setHeaderTitle("Input Hero name");

        TextField txtHeroName = new TextField("Hero name");
        txtHeroName.setValue("Hans");
        dialog.add(txtHeroName);

        Button saveButton = new Button("Ok");
        saveButton.addClickListener(buttonClickEvent ->
        {
            dialog.close();

            Hero hero = new Hero(txtHeroName.getValue());
            this.game = new Game(hero, new Story(new ArrayList<>()));
            this.gamePersistenceService.saveGameAndEnsureGameId(this.game);

            this.heroCommunicator = new HeroUiCommunicator(
                    game, this.prbHealth, this.prbMana, this.spnInventory, this
            );

            this.chatModel = aiService.createChatModelWithMemory();

            generateNewStoryPart(INITIAL_STORY_PROMPT.formatted(hero.getName()));
        });
        dialog.getFooter().add(saveButton);
        dialog.open();
    }

    record ProgressBarComponentPair(ProgressBar progressBar, Component component) {
    }
}
