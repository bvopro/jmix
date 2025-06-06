/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.kit.component.button;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import jakarta.annotation.Nullable;

import java.beans.PropertyChangeEvent;
import java.util.Objects;

/**
 * Delegate that handles action set to {@link JmixButton}.
 */
public class JmixButtonActionSupport {

    protected final JmixButton button;

    protected Action action;

    protected Registration buttonClickRegistration;
    protected Registration actionPropertyChangeRegistration;

    public JmixButtonActionSupport(JmixButton button) {
        this.button = button;
    }

    /**
     * Sets the action to the component. If {@code overrideComponentProperties} is
     * {@code true} then the action properties will override component's properties,
     * otherwise the component's properties will be overridden if they are {@code null}.
     * <p>
     * List of properties that the action and the component have and which can be overridden:
     * <ul>
     *     <li>{@code text}</li>
     *     <li>{@code description}</li>
     *     <li>{@code shortcutCombination}</li>
     *     <li>{@code enabled}</li>
     *     <li>{@code visible}</li>
     *     <li>{@code icon}</li>
     *     <li>{@code variant}</li>
     * </ul>
     *
     * @param action                      an action to set
     * @param overrideComponentProperties whether action properties override component properties
     */
    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null) {
            updateEnabled();
            updateVisible();
            updateText(overrideComponentProperties);
            updateTitle(overrideComponentProperties);
            updateIcon(overrideComponentProperties);
            updateActionVariant(overrideComponentProperties);

            buttonClickRegistration =
                    button.addClickListener(this::onButtonClick);
            actionPropertyChangeRegistration =
                    action.addPropertyChangeListener(this::onActionPropertyChange);
        }
    }

    /**
     * @return an action or {@code null}
     */
    @Nullable
    public Action getAction() {
        return action;
    }

    protected void addActionVariant(JmixButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.addThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeActionVariant(JmixButton component, ActionVariant actionVariant) {
        switch (actionVariant) {
            case PRIMARY:
                component.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                break;
            case DANGER:
                component.removeThemeVariants(ButtonVariant.LUMO_ERROR);
                break;
            case SUCCESS:
                component.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
                break;
            default:
        }
    }

    protected void removeRegistrations() {
        if (this.action != null) {
            if (buttonClickRegistration != null) {
                buttonClickRegistration.remove();
                buttonClickRegistration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected void updateText(boolean overrideComponentProperties) {
        if (Strings.isNullOrEmpty(button.getText()) || overrideComponentProperties) {
            button.setText(action.getText());
        }
    }

    protected void updateEnabled() {
        button.setEnabled(action.isEnabled());
    }

    protected void updateVisible() {
        button.setVisible(action.isVisible());
    }

    protected void updateActionVariant(boolean overrideComponentProperties) {
        if (overrideComponentProperties) {
            addActionVariant(button, action.getVariant());
        }
    }

    protected void updateIcon(boolean overrideComponentProperties) {
        if (button.getIcon() == null || overrideComponentProperties) {
            button.setIcon(action.getIcon());
        }
    }

    protected void updateTitle(boolean overrideComponentProperties) {
        if (Strings.isNullOrEmpty(button.getTitle()) || overrideComponentProperties) {
            button.setTitle(action.getDescription());
        }
    }

    protected void onButtonClick(ClickEvent<Button> event) {
        this.action.actionPerform(event.getSource());
    }

    protected void onActionPropertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Action.PROP_TEXT:
                button.setText((String) event.getNewValue());
                break;
            case Action.PROP_ENABLED:
                button.setEnabled((Boolean) event.getNewValue());
                break;
            case Action.PROP_VISIBLE:
                button.setVisible((Boolean) event.getNewValue());
                break;
            case Action.PROP_ICON:
                button.setIcon((Icon) event.getNewValue());
                break;
            case Action.PROP_DESCRIPTION:
                button.setTitle((String) event.getNewValue());
                break;
            case Action.PROP_VARIANT:
                removeActionVariant(button, (ActionVariant) event.getOldValue());
                addActionVariant(button, (ActionVariant) event.getNewValue());
                break;
            default:
        }
    }
}
