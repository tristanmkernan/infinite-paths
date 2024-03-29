/**
 Infinite Paths : Infinite Tower Defense Game With User Created Maps
 Copyright (C) 2015 Tristan Kernan

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.noviden.towerdefense.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import net.noviden.towerdefense.GameSettings;
import net.noviden.towerdefense.TowerDefense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class GameSettingsScreen implements Screen {
    final TowerDefense _towerDefense;

    private Stage _mainStage;

    // these values maintain `state` of setting custom shortcuts
    private Label _currentlySelectedActionLabel;
    private TextButton _currentlySelectedShortcut;
    private int _previouslySelectedKey;

    private HashMap<Integer, GameSettings.Actions> _modifiedKeyboardShortcutsMap;

    public GameSettingsScreen(final TowerDefense pTowerDefense) {
        _towerDefense = pTowerDefense;

        _modifiedKeyboardShortcutsMap = new HashMap<Integer, GameSettings.Actions>();

        _currentlySelectedActionLabel = null;
        _currentlySelectedShortcut = null;
        _previouslySelectedKey = -1;

        Skin skin = new Skin(Gdx.files.internal("assets/uiskin.json"));

        _mainStage = new Stage();

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        _mainStage.addActor(rootTable);

        // set up ui elements
        Label musicVolumeLabel = new Label("Music Volume", skin);
        final Slider musicVolumeSlider = new Slider(0.0f, 1.0f, 0.1f, false, skin);
        musicVolumeSlider.setValue(GameSettings.getMusicVolume());

        Label sfxVolumeLabel = new Label("Sound Effects Volume", skin);
        final Slider sfxVolumeSlider = new Slider(0.0f, 1.0f, 0.1f, false, skin);
        sfxVolumeSlider.setValue(GameSettings.getSoundVolume());

        Table volumeTable = new Table();
        volumeTable.add(musicVolumeLabel);
        volumeTable.add(musicVolumeSlider);
        volumeTable.row();
        volumeTable.add(sfxVolumeLabel);
        volumeTable.add(sfxVolumeSlider).pad(8.0f);

        Table keyboardShortcutsTable = new Table();

        HashMap<Integer, GameSettings.Actions> currentShortcutMap = GameSettings.getShortcutMap();

        // for sorting the settings on screen
        Collection<GameSettings.Actions> values = currentShortcutMap.values();
        ArrayList<String> valueNames = new ArrayList<String>();
        for (GameSettings.Actions action : values) {
            valueNames.add(action.toString());
        }

        Collections.sort(valueNames);

        for (String s : valueNames) {
            final Label actionLabel = new Label(s, skin);
            final TextButton shortcutButton = new TextButton(
                    Input.Keys.toString(GameSettings.getReverse(
                            GameSettings.Actions.valueOf(s)
                    )), skin);

            keyboardShortcutsTable.add(actionLabel).pad(10.0f);
            keyboardShortcutsTable.add(shortcutButton);
            keyboardShortcutsTable.row();

            shortcutButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {

                    if (_currentlySelectedShortcut == null) {
                        _currentlySelectedActionLabel = actionLabel;
                        _currentlySelectedShortcut = shortcutButton;
                        _previouslySelectedKey =
                                Input.Keys.valueOf(shortcutButton.getText().toString());

                        shortcutButton.setText("Press any un-set key!");
                    }
                }
            });
        }

        ScrollPane keyboardShortcutsScrollPane = new ScrollPane(keyboardShortcutsTable);
        keyboardShortcutsScrollPane.layout();

        // add in save and exit buttons
        Table exitFunctionalityTable = new Table();

        TextButton saveAndExitButton = new TextButton("Save and Exit", skin);
        TextButton exitWithoutSavingButton = new TextButton("Exit Without Saving", skin);

        exitFunctionalityTable.add(saveAndExitButton).pad(10.0f);
        exitFunctionalityTable.add(exitWithoutSavingButton);

        // finally, construct display table
        rootTable.add(volumeTable);
        rootTable.row();
        rootTable.add(keyboardShortcutsScrollPane);
        rootTable.row();
        rootTable.add(exitFunctionalityTable);

        InputMultiplexer multiplexer = new InputMultiplexer(_mainStage, new MyInputProcessor());

        Gdx.input.setInputProcessor(multiplexer);

        // add in event listeners here

        exitWithoutSavingButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                _towerDefense.setScreen(new net.noviden.towerdefense.Screens.MainMenuScreen(_towerDefense));
            }
        });

        saveAndExitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // attempt to save settings

                GameSettings.setMusicVolume(musicVolumeSlider.getPercent());

                GameSettings.setSoundVolume(sfxVolumeSlider.getPercent());

                // save keyboard shortcuts
                GameSettings.putShortcuts(_modifiedKeyboardShortcutsMap);

                // return to main menu
                _towerDefense.setScreen(new net.noviden.towerdefense.Screens.MainMenuScreen(_towerDefense));

                dispose();
            }
        });
    }

    public void render(float pDelta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _mainStage.act();
        _mainStage.draw();
    }

    public void pause() {}
    public void resume() {}

    public void show() {}
    public void hide() {}

    public void resize(int width, int height) {}
    public void dispose() {}

    private class MyInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {

            if (keycode < 0) {
                // keycode cannot be negative for Keys.toString()
                return false;
            }

            if (keycode == Input.Keys.ESCAPE) {
                _currentlySelectedShortcut.setText(Input.Keys.toString(_previouslySelectedKey));
                _currentlySelectedShortcut = null;
                _currentlySelectedActionLabel = null;
                _previouslySelectedKey = 0;
            }

            // attempt to set keyboard shortcut
            if (_currentlySelectedShortcut != null) {

                if (keycode != _previouslySelectedKey &&
                        (_modifiedKeyboardShortcutsMap.containsKey(keycode) ||
                        GameSettings.getShortcutAction(keycode) != null)) {
                    // don't set anything, map already contains this keycode

                    return false;
                }

                _currentlySelectedShortcut.setText(Input.Keys.toString(keycode));

                _modifiedKeyboardShortcutsMap.put(keycode,
                        GameSettings.Actions.valueOf(
                                _currentlySelectedActionLabel.getText().toString()));

                _currentlySelectedShortcut = null;
            }

            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer,
                                 int button) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {


            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(int amount) {

            return false;
        }
    }
}
