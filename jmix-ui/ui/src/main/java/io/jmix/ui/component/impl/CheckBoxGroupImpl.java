/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.impl;

import com.vaadin.server.Resource;
import com.vaadin.ui.DescriptionGenerator;
import io.jmix.core.MetadataTools;
import io.jmix.ui.component.CheckBoxGroup;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.DataAwareComponentsTools;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.meta.OptionsBinding;
import io.jmix.ui.component.data.options.OptionsBinder;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.widget.JmixCheckBoxGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.ui.component.impl.ComboBoxImpl.NULL_ITEM_ICON_GENERATOR;

public class CheckBoxGroupImpl<V> extends AbstractField<JmixCheckBoxGroup<V>, Set<V>, Collection<V>>
        implements CheckBoxGroup<V>, InitializingBean {

    public static final DescriptionGenerator NULL_ITEM_DESCRIPTION_GENERATOR = item -> null;

    /* Beans */
    protected MetadataTools metadataTools;
    protected IconResolver iconResolver;

    protected OptionsBinding<V> optionsBinding;

    protected Function<? super V, String> optionDescriptionProvider;
    protected Function<? super V, String> optionCaptionProvider;
    protected Function<? super V, String> optionIconProvider;

    public CheckBoxGroupImpl() {
        component = createComponent();

        attachValueChangeListener(component);
    }

    private JmixCheckBoxGroup<V> createComponent() {
        return new JmixCheckBoxGroup<>();
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixCheckBoxGroup<V> component) {
        component.setItemCaptionGenerator(this::generateItemCaption);
    }

    @Autowired
    protected void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    protected void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    @Nullable
    protected String generateItemCaption(@Nullable V item) {
        if (item == null) {
            return null;
        }

        if (optionCaptionProvider != null) {
            return optionCaptionProvider.apply(item);
        }

        return generateDefaultItemCaption(item);
    }

    // TODO: gg, refactor, extract to some value provider?
    protected String generateDefaultItemCaption(V item) {
        if (valueBinding != null && valueBinding.getSource() instanceof EntityValueSource) {
            EntityValueSource entityValueSource = (EntityValueSource) valueBinding.getSource();
            return metadataTools.format(item, entityValueSource.getMetaPropertyPath().getMetaProperty());
        }

        return metadataTools.format(item);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public Orientation getOrientation() {
        return WrapperUtils.convertToOrientation(component.getOrientation());
    }

    @Override
    public void setOrientation(Orientation orientation) {
        component.setOrientation(WrapperUtils.convertToVaadinOrientation(orientation));
    }

    @Override
    protected Set<V> convertToPresentation(@Nullable Collection<V> modelValue) throws ConversionException {
        return new LinkedHashSet<>(CollectionUtils.isNotEmpty(modelValue)
                ? modelValue
                : Collections.emptySet());
    }

    @Override
    protected Collection<V> convertToModel(@Nullable Set<V> componentRawValue) throws ConversionException {
        if (valueBinding != null) {
            Class<?> targetType = valueBinding.getSource().getType();

            if (List.class.isAssignableFrom(targetType)) {
                return new ArrayList<>(componentRawValue != null
                        ? componentRawValue
                        : Collections.emptyList());
            } else if (Set.class.isAssignableFrom(targetType)) {
                return new LinkedHashSet<>(componentRawValue != null
                        ? componentRawValue
                        : Collections.emptySet());
            }
        }

        return new LinkedHashSet<>(componentRawValue != null
                ? componentRawValue
                : Collections.emptySet());
    }

    @Nullable
    @Override
    public Options<V> getOptions() {
        return optionsBinding != null ? optionsBinding.getSource() : null;
    }

    @Override
    public void setOptions(@Nullable Options<V> options) {
        if (this.optionsBinding != null) {
            this.optionsBinding.unbind();
            this.optionsBinding = null;
        }

        if (options != null) {
            OptionsBinder optionsBinder = applicationContext.getBean(OptionsBinder.class);
            this.optionsBinding = optionsBinder.bind(options, this, this::setItemsToPresentation);
            this.optionsBinding.activate();
        }
    }

    @Override
    protected void valueBindingConnected(ValueSource<Collection<V>> valueSource) {
        super.valueBindingConnected(valueSource);

        if (valueSource instanceof EntityValueSource) {
            DataAwareComponentsTools dataAwareComponentsTools = applicationContext.getBean(DataAwareComponentsTools.class);
            dataAwareComponentsTools.setupOptions(this, (EntityValueSource) valueSource);
        }
    }

    protected void setItemsToPresentation(Stream<V> options) {
        Set<V> oldValue = component.getValue();

        List<V> newOptions = options.collect(Collectors.toList());
        component.setItems(newOptions);

        Set<V> newValue = newOptions.stream()
                .filter(oldValue::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        component.setValue(newValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionDescriptionProvider(@Nullable Function<? super V, String> optionDescriptionProvider) {
        if (this.optionDescriptionProvider != optionDescriptionProvider) {
            this.optionDescriptionProvider = optionDescriptionProvider;

            if (optionDescriptionProvider != null) {
                component.setItemDescriptionGenerator(optionDescriptionProvider::apply);
            } else {
                component.setItemDescriptionGenerator(NULL_ITEM_DESCRIPTION_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionDescriptionProvider() {
        return optionDescriptionProvider;
    }

    @Override
    public void setOptionCaptionProvider(@Nullable Function<? super V, String> optionCaptionProvider) {
        if (this.optionCaptionProvider != optionCaptionProvider) {
            this.optionCaptionProvider = optionCaptionProvider;

            // reset item captions
            component.setItemCaptionGenerator(this::generateItemCaption);
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionCaptionProvider() {
        return optionCaptionProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setOptionIconProvider(@Nullable Function<? super V, String> optionIconProvider) {
        if (this.optionIconProvider != optionIconProvider) {
            this.optionIconProvider = optionIconProvider;

            if (optionIconProvider != null) {
                component.setItemIconGenerator(this::generateOptionIcon);
            } else {
                component.setItemIconGenerator(NULL_ITEM_ICON_GENERATOR);
            }
        }
    }

    @Nullable
    @Override
    public Function<? super V, String> getOptionIconProvider() {
        return optionIconProvider;
    }

    @Nullable
    protected Resource generateOptionIcon(V item) {
        String resourceId;
        try {
            resourceId = optionIconProvider.apply(item);
        } catch (Exception e) {
            LoggerFactory.getLogger(CheckBoxGroupImpl.class)
                    .warn("Error invoking optionIconProvider apply method", e);
            return null;
        }

        return iconResolver.getIconResource(resourceId);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                || CollectionUtils.isEmpty(getValue());
    }
}
