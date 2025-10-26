package gt.skynet.semvis.util;

import gt.skynet.semvis.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserName {

    @Autowired
    private UserRepo userRepo;

    public String generateFrom(String base) {
        String root = base == null ? "user" : base.toLowerCase()
                .replaceAll("[^a-z0-9._-]", "")
                .replaceAll("_{2,}", "_");
        if (root.length() < 3) root = (root + "user").substring(0, 4);

        String candidate = root;
        int i = 0;
        while (userRepo.findByUsernameIgnoreCase(candidate).isPresent()) {
            i++;
            candidate = (root + i);
            if (candidate.length() > 30) candidate = candidate.substring(0, 30);
        }
        return candidate;
    }
}
