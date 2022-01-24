package alex.beta.portablecinema.command;

import alex.beta.portablecinema.PortableCinemaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command<T> {
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);

    public abstract T execute(PortableCinemaConfig config);
}