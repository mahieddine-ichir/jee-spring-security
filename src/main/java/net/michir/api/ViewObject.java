package net.michir.api;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by michir on 30/04/2018.
 */
public class ViewObject {

    private String role;

    private String id = UUID.randomUUID().toString();

    private Date creationDate = Calendar.getInstance().getTime();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static ViewObject of() {
        ViewObject viewObject = new ViewObject();
        ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails()).getAuthorities().forEach(a -> viewObject.setRole(a.getAuthority()));
        return viewObject;
    }

    public static ViewObject ofUser() {
        ViewObject viewObject = new ViewObject();
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().forEach(
                e -> viewObject.setRole(e.getAuthority())
        );
        return viewObject;
    }

    public static ViewObject ofApp() {
        ViewObject viewObject = new ViewObject();
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().forEach(
                e -> viewObject.setRole(e.getAuthority())
        );
        return viewObject;
    }
}
