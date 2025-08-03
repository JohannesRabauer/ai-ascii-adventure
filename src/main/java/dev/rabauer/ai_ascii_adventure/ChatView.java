package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.rabauer.ai_ascii_adventure.ai.AiService;
import dev.rabauer.ai_ascii_adventure.ai.Assistant;
import dev.rabauer.ai_ascii_adventure.ai.HeroUiCommunicator;
import dev.rabauer.ai_ascii_adventure.dto.Game;
import dev.rabauer.ai_ascii_adventure.dto.Hero;
import dev.rabauer.ai_ascii_adventure.dto.Story;
import dev.rabauer.ai_ascii_adventure.dto.StoryPart;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static dev.rabauer.ai_ascii_adventure.ai.AiService.CREATE_ASCII_ART_PROMPT;
import static dev.rabauer.ai_ascii_adventure.ai.AiService.EXTRACT_IMAGE_TITLE_PROMPT;
import static dev.rabauer.ai_ascii_adventure.dto.Game.INITIAL_PROMPT;

@Route(value = "", layout = MainLayout.class)
public class ChatView extends SplitLayout implements GameManager {


    private final TextArea txtStory = new TextArea();
    private final TextArea txtAsciiArt = new TextArea();

    private final Assistant chatModel;
    private final AiService aiService;
    private ProgressBar prbHealth;
    private ProgressBar prbMana;
    private HeroUiCommunicator heroCommunicator;
    private Span spnInventory;
    private Game game;

    @Autowired
    public ChatView(AiService aiService) {
        this.aiService = aiService;
        this.chatModel = aiService.createChatModel(true); // LangChain4J model creation

        this.setSizeFull();
        this.addToPrimary(createAsciiArt());
        this.addToSecondary(createStoryComponent());

        openStartDialog();
    }

    private void openStartDialog() {
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
            this.heroCommunicator = new HeroUiCommunicator(
                    hero, this.prbHealth, this.prbMana, this.spnInventory, this
            );

            this.game = new Game(hero, new Story(new ArrayList<>()));

            generateNewStoryPart(INITIAL_PROMPT.formatted(hero.getName()));
        });
        dialog.getFooter().add(saveButton);
        dialog.open();
    }

    public void showGameOver(boolean fail) {
        UI.getCurrent().access(() ->
        {
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
        });
    }

    private Component createAsciiArt() {
        txtAsciiArt.setSizeFull();
        txtAsciiArt.setTitle("Graphics");
        txtAsciiArt.getStyle().set("font-family", "'Courier New', monospace");

        prbHealth = new ProgressBar(0, 1, 1);
        prbHealth.setWidthFull();
        prbMana = new ProgressBar(0, 1, 0.5);
        prbMana.setWidthFull();

        HorizontalLayout hlStatusBar = new HorizontalLayout(prbHealth, prbMana);
        hlStatusBar.setWidthFull();

        spnInventory = new Span();
        spnInventory.setWidthFull();
        spnInventory.setHeight("100px");
        HorizontalLayout hlInventory = new HorizontalLayout(spnInventory);
        hlInventory.setHeight("100px");
        hlInventory.setWidthFull();

        return new VerticalLayout(txtAsciiArt, hlStatusBar, hlInventory);
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
        aiService.generateNewStoryPart(
                this.chatModel,
                textPrompt,
                HeroUiCommunicator.class,
                newText ->
                        current.access(
                                () ->
                                        txtStory.setValue(txtStory.getValue() + newText)
                        ),
                completeResponse -> current.access(() -> handleFinishedStoryPart(completeResponse.aiMessage().text()))
        );
    }

    private void handleFinishedStoryPart(String storyPartAsString) {
        StoryPart storyPart = new StoryPart(storyPartAsString);
        this.game.story().storyParts().add(storyPart);

        txtAsciiArt.clear();

        UI current = UI.getCurrent();
        aiService.generateAsciiArt(
                aiService.createChatModel  (false),
                EXTRACT_IMAGE_TITLE_PROMPT.formatted(storyPartAsString),
                responseWithTitle -> {
                    current.access(() -> txtAsciiArt.setTitle(responseWithTitle));
                    aiService.generateAsciiArt(
                            aiService.createChatModel(false),
                            CREATE_ASCII_ART_PROMPT.formatted(responseWithTitle),
                            response -> current.access(() -> txtAsciiArt.setValue(response))
                    );
                }
        );
    }
}
