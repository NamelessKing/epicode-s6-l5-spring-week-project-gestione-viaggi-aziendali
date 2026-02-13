package it.epicode.gestioneviaggiaziendali.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import it.epicode.gestioneviaggiaziendali.exception.ConflictException;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("File avatar mancante");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            Object url = result.get("secure_url");
            if (url == null) {
                url = result.get("url");
            }
            return url == null ? "" : url.toString();
        } catch (IOException ex) {
            throw new ConflictException("Errore durante l'upload dell'immagine");
        }
    }
}
