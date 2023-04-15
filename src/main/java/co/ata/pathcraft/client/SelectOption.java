package co.ata.pathcraft.client;

import net.minecraft.text.Text;

public class SelectOption<T> {
    public T value;
    public Text name;
    public Text description;
    public boolean disabled = false;

    public SelectOption(T value, Text name, Text description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }
}
