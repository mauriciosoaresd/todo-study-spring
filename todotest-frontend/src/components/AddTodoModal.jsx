import { useEffect, useState } from "react";
import Modal from "react-bootstrap/Modal";

function AddTodoModal({ getTodos, setTodos }) {
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState();

  const [date, setDate] = useState();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState(1);

  const handleShow = () => setShow(true);
  const handleClose = () => {
    setShow(false);
    setDate();
    setTitle("");
    setDescription("");
  };

  const sendForm = async (ev) => {
    ev.preventDefault();
    setLoading(true);

    fetch("/api/todo", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title: title,
        description: description,
        targetDate: date,
        priority: priority,
      }),
    }).then(async (res) => {
      const response = await res.json();
      if (res.ok) {
        setTodos((prevState) => prevState.concat(response));
        setLoading(false);
        handleClose();
      } else {
        console.error(response)
        setErrorMessage(response.message);
        setLoading(false);
      }
    });
  };

  useEffect(() => {
    return () => {};
  }, []);

  return (
    <>
      <button className="rounded" onClick={handleShow}>
        Add To Do
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
              onChange={(ev) => setTitle(ev.target.value)}
            />

            <label>Description</label>
            <textarea
              name="description"
              minLength={10}
              maxLength={200}
              required
              onChange={(ev) => setDescription(ev.target.value)}
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
              onChange={(ev) => setDate(ev.target.value)}
            />
            <label>Priority</label>

            <select
              id="priorty"
              onChange={(ev) => setPriority(ev.target.value)}
            >
              <option value="1">1</option>
              <option value="2">2</option>
              <option value="3">3</option>
              <option value="4">4</option>
            </select>

            {errorMessage ? <p className="text-danger">{errorMessage}</p> : ""}

            <Modal.Footer>
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

export default AddTodoModal;
