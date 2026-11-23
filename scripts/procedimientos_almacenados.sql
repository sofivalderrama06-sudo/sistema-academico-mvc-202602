-- ============================================================================
-- PROCEDIMIENTOS ALMACENADOS Y FUNCIONES - SISTEMA ACADÉMICO UNIAJC
-- Esquema: "practica-mvc"
-- Motor: PostgreSQL 11+
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PROCEDIMIENTO 1: INSERTAR INSCRIPCIÓN (MATRÍCULA DE ESTUDIANTE)
-- Operación: INSERT con validaciones de existencia, rango y no duplicidad
-- ----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE "practica-mvc".sp_inscribir_estudiante(
    IN p_id_estudiante INT,
    IN p_id_grupo INT,
    IN p_final_grade NUMERIC DEFAULT 0.0,
    IN p_status VARCHAR DEFAULT 'ACTIVO'
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_existe_estudiante BOOLEAN;
    v_existe_grupo BOOLEAN;
    v_ya_inscrito BOOLEAN;
BEGIN
    -- 1. Validar que los IDs sean enteros positivos
    IF p_id_estudiante <= 0 THEN
        RAISE EXCEPTION 'El identificador del estudiante debe ser mayor que cero.';
    END IF;

    IF p_id_grupo <= 0 THEN
        RAISE EXCEPTION 'El identificador del grupo debe ser mayor que cero.';
    END IF;

    -- 2. Validar que el estudiante exista en la tabla estudiantes
    SELECT EXISTS(
        SELECT 1 FROM "practica-mvc".estudiantes WHERE id = p_id_estudiante
    ) INTO v_existe_estudiante;

    IF NOT v_existe_estudiante THEN
        RAISE EXCEPTION 'El estudiante con ID % no existe en la base de datos.', p_id_estudiante;
    END IF;

    -- 3. Validar que el grupo exista en la tabla grupos
    SELECT EXISTS(
        SELECT 1 FROM "practica-mvc".grupos WHERE id = p_id_grupo
    ) INTO v_existe_grupo;

    IF NOT v_existe_grupo THEN
        RAISE EXCEPTION 'El grupo con ID % no existe en la base de datos.', p_id_grupo;
    END IF;

    -- 4. Validar que el estudiante no esté matriculado ya en este grupo (Regla UNIQUE)
    SELECT EXISTS(
        SELECT 1 FROM "practica-mvc".inscripciones 
        WHERE id_estudiante = p_id_estudiante AND id_grupo = p_id_grupo
    ) INTO v_ya_inscrito;

    IF v_ya_inscrito THEN
        RAISE EXCEPTION 'El estudiante con ID % ya se encuentra matriculado en el grupo %.', p_id_estudiante, p_id_grupo;
    END IF;

    -- 5. Validar rango de nota (0.0 a 5.0)
    IF p_final_grade < 0.0 OR p_final_grade > 5.0 THEN
        RAISE EXCEPTION 'La nota final (%) debe estar en el rango de 0.0 a 5.0.', p_final_grade;
    END IF;

    -- 6. Validar estado permitido
    IF UPPER(p_status) NOT IN ('ACTIVO', 'APROBADO', 'REPROBADO', 'CANCELADO') THEN
        RAISE EXCEPTION 'Estado no permitido (%). Debe ser ACTIVO, APROBADO, REPROBADO o CANCELADO.', p_status;
    END IF;

    -- 7. Inserción del registro
    INSERT INTO "practica-mvc".inscripciones (
        id_estudiante,
        id_grupo,
        final_grade,
        status,
        created_at
    ) VALUES (
        p_id_estudiante,
        p_id_grupo,
        p_final_grade,
        UPPER(p_status),
        CURRENT_TIMESTAMP
    );

    RAISE NOTICE 'Estudiante % inscrito exitosamente en el grupo %.', p_id_estudiante, p_id_grupo;
END;
$$;


-- ----------------------------------------------------------------------------
-- PROCEDIMIENTO 2: ELIMINACIÓN SEGURA DE INSCRIPCIÓN
-- Operación: DELETE con validación previa de integridad institucional
-- ----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE "practica-mvc".sp_eliminar_inscripcion_segura(
    IN p_id_inscripcion INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_actual VARCHAR(20);
BEGIN
    -- 1. Consultar estado actual
    SELECT status INTO v_estado_actual
    FROM "practica-mvc".inscripciones
    WHERE id = p_id_inscripcion;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No se puede eliminar: la inscripción % no existe.', p_id_inscripcion;
    END IF;

    -- 2. Regla de integridad: No se permite borrar inscripciones que ya fueron aprobadas
    IF v_estado_actual = 'APROBADO' THEN
        RAISE EXCEPTION 'Operación denegada: No se puede eliminar una inscripción que ya ha sido APROBADA en el historial académico.';
    END IF;

    -- 3. Proceder al borrado si el estado es ACTIVO, REPROBADO o CANCELADO
    DELETE FROM "practica-mvc".inscripciones
    WHERE id = p_id_inscripcion;

    RAISE NOTICE 'Inscripción % eliminada correctamente.', p_id_inscripcion;
END;
$$;


-- ----------------------------------------------------------------------------
-- FUNCIÓN 3: CONSULTAR GRUPOS PARA COMBOBOX
-- Operación: CONSULTA con JOIN para alimentar componentes visuales JComboBox
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION "practica-mvc".fn_consultar_grupos_combobox()
RETURNS TABLE (
    id_grupo INT,
    id_materia INT,
    nombre_materia VARCHAR(100),
    id_docente INT,
    nombre_docente VARCHAR(100),
    aula VARCHAR(50),
    horario VARCHAR(100),
    texto_combobox TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        g.id AS id_grupo,
        m.id AS id_materia,
        m.name AS nombre_materia,
        d.id AS id_docente,
        d.name AS nombre_docente,
        g.classroom AS aula,
        g.schedule AS horario,
        -- Texto formateado listo para mostrar en el ComboBox de la interfaz gráfica
        CONCAT(m.name, ' - Prof. ', d.name, ' [Aula: ', g.classroom, ' | ', g.schedule, ']') AS texto_combobox
    FROM "practica-mvc".grupos g
    INNER JOIN "practica-mvc".materias m ON g.id_materia = m.id
    INNER JOIN "practica-mvc".docentes d ON g.id_docente = d.id
    ORDER BY m.name ASC, g.id ASC;
END;
$$;
