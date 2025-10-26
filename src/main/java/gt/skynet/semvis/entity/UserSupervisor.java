package gt.skynet.semvis.entity;

import gt.skynet.semvis.entity.user.User;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_supervisor")
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = {"supervisor", "tecnico"})
public class UserSupervisor {

    @EmbeddedId
    private UserSupervisorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supervisorId")
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tecnicoId")
    @JoinColumn(name = "tecnico_id")
    private User tecnico;
}