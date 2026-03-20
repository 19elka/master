CREATE OR REPLACE FUNCTION public.prevent_isbn_update()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.isbn IS NOT NULL AND NEW.isbn != OLD.isbn THEN
        RAISE EXCEPTION 'ISBN cannot be changed once it is set';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER edition_isbn_immutable
    BEFORE UPDATE ON public.editions
    FOR EACH ROW
    EXECUTE FUNCTION public.prevent_isbn_update();
