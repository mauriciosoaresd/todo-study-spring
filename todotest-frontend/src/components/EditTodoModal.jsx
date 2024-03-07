import { useEffect, useState } from "react";
import Modal from "react-bootstrap/Modal";

function EditTodoModal({ todo, updateTodo }) {
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState();

  const [todoState, setTodoState] = useState(todo);

  const handleShow = () => setShow(true);
  const handleClose = () => {
    setLoading(false);
    setTodoState(todo);
    setShow(false);
  };

  const sendForm = async (ev) => {
    ev.preventDefault();
    setLoading(true);

    let fieldDifferences = Object.keys(todo).filter(
      (key) => todo[key] !== todoState[key]
    );
    switch (fieldDifferences.length) {
      case 0:
        break;
      case Object.keys(todo).length - 1:
        fetch(`/api/todo/${todo.id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(todoState),
        }).then(async (res) => {
          if (res.ok) {
            updateTodo(await res.json());
            handleClose();
          } else {
            let jsonError = await res.json();
            setErrorMessage(jsonError.message);
            setLoading(false);
          }
        });
        break;
      default:
        let obj = {};
        fieldDifferences.map((field) => (obj[field] = todoState[field]));

        fetch(`/api/todo/${todo.id}`, {
          method: "PATCH",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(obj),
        }).then(async (res) => {
          const response = res.json();
          if (res.ok) {
            updateTodo(response);
            handleClose();
          } else {
            console.error(response)
            setErrorMessage(response);
          }
        });
        break;
    }
  };

  useEffect(() => {
    return () => {};
  }, []);

  return (
    <>
      <button className="rounded" onClick={handleShow}>
        Edit
      </button>

      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header closeButton>
          <Modal.Title>To do form</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <form className="d-flex flex-column" onSubmit={(ev) => sendForm(ev)}>
            <label>Title</label>
            <input
              type="text"
              name="title"
              minLength={2}
              maxLength={20}
              required
              value={todoState.title}
              onChange={(ev) =>
                setTodoState((prevState) => ({
                  ...prevState,
                  title: ev.target.value,
                }))
              }
            />

            <label>Description</label>
            <textarea
              name="description"
              minLength={5}
              maxLength={30}
              required
              value={todoState.description}
              onChange={(ev) =>
                setTodoState((prevState) => ({
                  ...prevState,
                  description: ev.target.value,
                }))
              }
            />

            <label>Target date</label>
            <input
              type="date"
              id="targetDate"
              min={
                new Date(Date.now() + 1 * 24 * 60 * 60 * 1000)
                  .toISOString()
                  .split("T")[0]
              }
              required
              value={todoState.targetDate}
              onChange={(ev) => {
                console.log(ev.target);
                setTodoState((prevState) => ({
                  ...prevState,
                  targetDate: ev.target.value,
                }));
              }}
            />
            <label>Priority</label>

            <select
              id="priorty"
              value={todoState.priority}
              onChange={(ev) =>
                setTodoState((prevState) => ({
                  ...prevState,
                  priority: parseInt(ev.target[ev.target.selectedIndex].text),
                }))
              }
            >
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="3">3</option>
              <option value="4">4</option>
            </select>

            <Modal.Footer>
            {errorMessage? <h1>{errorMessage}</h1>:""}
              <button type="button" className="rounded" onClick={handleClose}>
                Close
              </button>
              <button type="submit" className="rounded" disabled={loading}>
                {loading ? "Sending..." : "Send"}
              </button>
            </Modal.Footer>
          </form>
        </Modal.Body>
      </Modal>
    </>
  );
}

export default EditTodoModal;
