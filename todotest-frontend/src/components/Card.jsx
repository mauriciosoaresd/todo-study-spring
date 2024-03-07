import { useState } from "react";
import EditTodoModal from "./EditTodoModal";

function Card({ todo, updateTodo, setTodos }) {
  const [loading, setLoading] = useState(false);

  const removeTodo = () => {
    setLoading(true);
    fetch(`/api/todo/${todo.id}`, { method: "DELETE" }).then((res) => {
      setLoading(false);
      setTodos((prevState) => {
        let newArr = prevState.filter(
          (td) => JSON.stringify(td) != JSON.stringify(todo)
        );
        return newArr;
      });
    });
  };

  const priorityColors = {
    1: "primary",
    2: "info",
    3: "warning",
    4: "danger",
  };
  return (
    <div className={`card bg-${priorityColors[todo.priority]}`}>
      <div className="card-body">
        <h5 className="card-title">{todo.title}</h5>
        <p className="card-text">{todo.description}</p>
      </div>
      <p className="card-text mt-auto">
        <strong>Target date: </strong>
        {todo.targetDate}
      </p>
      <EditTodoModal todo={todo} updateTodo={updateTodo} />
      <button className="rounded mt-1" onClick={removeTodo} disabled={loading}>
        {loading ? "Removing..." : "Remove"}
      </button>
    </div>
  );
}
export default Card;
