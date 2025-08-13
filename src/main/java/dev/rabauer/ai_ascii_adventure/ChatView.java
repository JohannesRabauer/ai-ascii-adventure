package dev.rabauer.ai_ascii_adventure;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.router.Route;
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
    private final AiService aiService;
    private Assistant chatModel;
    private ProgressBar prbHealth;
    private ProgressBar prbMana;
    private HeroUiCommunicator heroCommunicator;
    private Span spnInventory;
    private Game game;

    @Autowired
    public ChatView(AiService aiService) {
        this.aiService = aiService;

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

            this.chatModel = aiService.createChatModel(
                    true,
                    this.heroCommunicator
            );

            this.game = new Game(hero, new Story(new ArrayList<>()));

            generateNewStoryPart(INITIAL_PROMPT.formatted(hero.getName()));
        });
        dialog.getFooter().add(saveButton);
        dialog.open();
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
        aiService.generateNewStoryPart(
                this.chatModel,
                textPrompt,
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
                aiService.createChatModel(false, null),
                EXTRACT_IMAGE_TITLE_PROMPT.formatted(storyPartAsString),
                responseWithTitle -> {
                    current.access(() -> txtAsciiArt.setTitle(responseWithTitle));
                    aiService.generateAsciiArt(
                            aiService.createChatModel(false, null),
                            CREATE_ASCII_ART_PROMPT.formatted(responseWithTitle),
                            response -> current.access(() -> txtAsciiArt.setValue(response))
                    );
                }
        );
    }

    record ProgressBarComponentPair(ProgressBar progressBar, Component component) {
    }
}
